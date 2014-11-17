package io.prediction.examples.stock

import org.saddle._
import org.saddle.index.IndexTime

import scala.collection.immutable.HashMap
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import com.github.nscala_time.time.Imports._

import scala.math

import nak.regress.LinearRegression


//test code change
class RegressionStrategy (
  trainingWindowSize: Int,
  shifts: Seq[Int],
  features: Seq[Series[ (Frame[DateTime, String, Double], Int) => Frame[DateTime, String, Double] ]])
    extends StockStrategy[Map[String, DenseVector[Double]]] {
  /*val trainingWindowSize = 200 // data time range in # of days
  val shifts = Seq(0, 1, 5, 22) // days used in regression model*/

  //Most minimal constructor? How to make sure length is correct?
  def this(features: Seq[Series[ (Frame[DateTime, String, Double], Int) => Frame[DateTime, String, Double] ]])) {
    this(200, (Array.fill(1)(0) ++ Array.fill(features.length)(1)).toSeq, features)
  }

  private def getRet(logPrice: Frame[DateTime, String, Double], d: Int) =
    (logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))

  private def regress(
    calculatedData: Seq[Series[DateTime, Double]],
    retF1d: Series[DateTime, Double]) = {
    val array = (
      calculatedData.map(_.toVec.contents).reduce(_ ++ _) ++
      Array.fill(retF1d.length)(1.0))
    val target = DenseVector[Double](retF1d.toVec.contents)
    val m = DenseMatrix.create[Double](retF1d.length, calculatedData.length + 1, array)
    val result = LinearRegression.regress(m, target)
    result
  }

  /* Train */
  def createModel(dataView: DataView): Map[String, DenseVector[Double]] = {
    // trainingWindowSize - data time range
    val price = dataView.priceFrame(trainingWindowSize) // map from ticker to array of stock prices
    val logPrice = price.mapValues(math.log)
    val active = dataView.activeFrame(trainingWindowSize) // what is activeFrame?

    //PASS THIS IN AS PARAM
    /* Calling Indicator class */
    //println("RegressionStrategy: calling calcRSI")
    //val indic = new Indicators()
    //println("RegressionStrategy: finished calling calcRSI")


    // Calculate data with corresponding features
    var x = 0
    var retSeq = Seq[Frame[DateTime,String,Double]]()
    for (x <- 1 to shifts.length - 1) {
      retSeq = retSeq ++ Seq(features(x)(logPrice, shifts(x)))
    }

    //Calculate target data returns
    val retF1d = getRet(logPrice, -1)

    //DEFINE AS CONSTANTS AT THE TOP
    //OR USE THE MAX OF SHIFTS
    val timeIndex = price.rowIx // WHAT IS ROWIX ???
    //val firstIdx = 25 // why start on 25th? -> only data past offset of 22 matters
    val firstIdx = shifts.max + 3
    val lastIdx = timeIndex.length

    // What is this?
    val tickers = price.colIx.toVec.contents

    // Why are you using the first column? What is findOne?
    // Is this a validity test for the data set - filtering out the non-active tickers?
    // returns a 1D array - time series
    val tickerModelMap = tickers
    .filter(ticker => (active.firstCol(ticker).findOne(_ == false) == -1))
    .map(ticker => {
      val model = regress(
        // Only pass in relevant data
        retSeq.map(s => s.firstCol(ticker).slice(firstIdx, lastIdx)),
        retF1d.firstCol(ticker).slice(firstIdx, lastIdx))
      (ticker, model)
    }).toMap

    // What is this mapping the tickers to?
    tickerModelMap
  }

  private def predictOne(
    coef: DenseVector[Double],
    price: Series[DateTime, Double]): Double = {
    val sp = shifts
      .map(s => (s, math.log(price.raw(price.length - s - 1))))
      .toMap

    var densVecArray = Array[Double]();
    var x = 0
    for (x <- 1 to shifts.length - 1) {
      densVecArray = densVecArray ++ Array(sp(shifts(0)) - sp(shifts(x)))
    }

    densVecArray = densVecArray ++ Array(1)
    val vec = DenseVector[Double](densVecArray)

    //val vec = DenseVector[Double](
    //  sp(shifts(0)) - sp(shifts(1)),
    //  sp(shifts(0)) - sp(shifts(2)),
    //  sp(shifts(0)) - sp(shifts(3)),
    //  1)

    val p = coef.dot(vec)
    return p
  }

  /* Predict */
  def onClose(model: Map[String, DenseVector[Double]], query: Query)
  : Prediction = {
    val dataView = query.dataView

    val price = dataView.priceFrame(windowSize = 30)
    //val logPrice = price.mapValues(math.log)

    val prediction = query.tickers
      .filter(ticker => model.contains(ticker))
      .map { ticker => {
        val p = predictOne(
          model(ticker),
          price.firstCol(ticker))
        (ticker, p)
      }}

    Prediction(HashMap[String, Double](prediction:_*))
  }
}
