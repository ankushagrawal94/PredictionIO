package io.prediction.examples.stock

import org.saddle._
import org.saddle.index.IndexTime

import scala.collection.immutable.HashMap
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import com.github.nscala_time.time.Imports._

import scala.math

import nak.regress.LinearRegression

class RegressionStrategy (trainingWindowSize: Int, shifts: Seq)
    extends StockStrategy[Map[String, DenseVector[Double]]] {
  //DATA FIELDS TO ADD
  //SERIES OF SEQUENCE (PASSED AS PARAM)
  //LENGHTH OF SERIES OF SEQUENCE FOR DENSEVECTOR
  //CREATE TRAINING WINDOW SIZE AND SHIFTS SEQ BASED ON PASSSED IN DATA

  //TODO F1D DOES THIS PARAMETER COMING FROM GETRED NEED TO BE MOVED OR STAY? iS IT STATIC OR NOT?

  val trainingWindowSize = 200 // data time range in # of days
  val shifts = Seq(0, 1, 5, 22) // days used in regression model

  //THIS NEEDS TO MOVE TO INDICATORS OR FEATURES
  // Difference in closing prices?
  private def getRet(logPrice: Frame[DateTime, String, Double], d: Int) =
    (logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))

  // Justin's regress
  // private def regress(
  //   ret1d: Series[DateTime, Double],
  //   ret1w: Series[DateTime, Double],
  //   ret1m: Series[DateTime, Double],
  //   retF1d: Series[DateTime, Double]) = {
  //   val array = (
  //     Seq(ret1d, ret1w, ret1m).map(_.toVec.contents).reduce(_ ++ _) ++
  //     Array.fill(ret1d.length)(1.0))
  //   val target = DenseVector[Double](retF1d.toVec.contents)
  //   val m = DenseMatrix.create[Double](ret1d.length, 4, array)
  //   val result = LinearRegression.regress(m, target)
  //   result
  // }

  // Generalized regression
  private def regress2(
    features: Seq[Series[DateTime, Double]],
    retF1d: Series[DateTime, Double]) = {
    val array = (
      features.map(_.toVec.contents).reduce(_ ++ _) ++
      Array.fill(retF1d.length)(1.0))
    val target = DenseVector[Double](retF1d.toVec.contents)
    val m = DenseMatrix.create[Double](retF1d.length, features.length + 1, array)
    val result = LinearRegression.regress(m, target)
    result
  }

  def createModel(dataView: DataView): Map[String, DenseVector[Double]] = {
    // trainingWindowSize - data time range
    val price = dataView.priceFrame(trainingWindowSize) // map from ticker to array of stock prices
    val logPrice = price.mapValues(math.log)
    val active = dataView.activeFrame(trainingWindowSize) // what is activeFrame?

    //PASS THIS IN AS PARAM
    /* Calling Indicator class */
    println("RegressionStrategy: calling calcRSI")
    val indic = new Indicators()
    // For loop for making calls to Indicators - fills a sequence with return values
    val rsi1d = indic.calcRSI(logPrice, shifts(1))
    val rsi1w = indic.calcRSI(logPrice, shifts(2))
    val rsi1m = indic.calcRSI(logPrice, shifts(3))
    // val rsiF1d = indic.calcRSI(logPrice, -1) // -1 is not handled by our RSI calculation
    println("RegressionStrategy: finished calling calcRSI")

    // Calculate feature
    var x = 0
    var retSeq = Seq[Frame[DateTime,String,Double]]()
    for (x <- 1 to shifts.length - 1) {
      retSeq = retSeq ++ Seq(getRet(logPrice, shifts(x)))
    }

    val ret1d = getRet(logPrice, shifts(1))
    val ret1w = getRet(logPrice, shifts(2))
    val ret1m = getRet(logPrice, shifts(3))
    val retF1d = getRet(logPrice, -1)
    //END OF SECTIONS TO TURN TO PARAMS

    //DEFINE AS CONSTANTS AT THE TOP
    //OR USE THE MAX OF SHIFTS
    val timeIndex = price.rowIx // WHAT IS ROWIX ???
    val firstIdx = 25 // why start on 25th? -> offset past 22
    val lastIdx = timeIndex.length

    // What is this?
    val tickers = price.colIx.toVec.contents

    // Why are you using the first column? What is findOne?
    // Is this a validity test for the data set - filtering out the non-active tickers?
    // returns a 1D array - time series
    val tickerModelMap = tickers
    .filter(ticker => (active.firstCol(ticker).findOne(_ == false) == -1))
    .map(ticker => {
      val model = regress2(
        // Seq(
        // rsi1d.firstCol(ticker).slice(firstIdx, lastIdx),
        // rsi1w.firstCol(ticker).slice(firstIdx, lastIdx),
        // rsi1m.firstCol(ticker).slice(firstIdx, lastIdx)),
        // ret1d.firstCol(ticker).slice(firstIdx, lastIdx),
        // ret1w.firstCol(ticker).slice(firstIdx, lastIdx),
        // ret1m.firstCol(ticker).slice(firstIdx, lastIdx)),
        // Get the tickers
        retSeq.map(s => s.firstCol(ticker).slice(firstIdx, lastIdx)),
        retF1d.firstCol(ticker).slice(firstIdx, lastIdx))
      (ticker, model)
    }).toMap

    // What is this mapping the tickers to?
    tickerModelMap
  }

  // Justin's predictOne
  // private def predictOne(
  //   coef: DenseVector[Double],
  //   price: Series[DateTime, Double]): Double = {
  //   val shifts = Seq(0, 1, 5, 22)
  //   val sp = shifts
  //     .map(s => (s, math.log(price.raw(price.length - s - 1))))
  //     .toMap

  //   val vec = DenseVector[Double](
  //     sp(0) - sp(1),
  //     sp(0) - sp(5),
  //     sp(0) - sp(22),
  //     1)

  //   val p = coef.dot(vec)
  //   return p
  // }

  // General predictOne
  private def predictOne2(
    coef: DenseVector[Double],
    price: Series[DateTime, Double]): Double = {
    val sp = shifts
      .map(s => (s, math.log(price.raw(price.length - s - 1))))
      .toMap

    //TURN INTO A LOOP.NEED TO ADD TO
    //GENERALIZE THE PARAMETERS AND PASS IN AFTER SHIFTS
    val vec = DenseVector[Double](
      sp(shifts(0)) - sp(shifts(1)),
      sp(shifts(0)) - sp(shifts(2)),
      sp(shifts(0)) - sp(shifts(3)),
      1)

    val p = coef.dot(vec)
    return p
  }

  def onClose(model: Map[String, DenseVector[Double]], query: Query)
  : Prediction = {
    val dataView = query.dataView

    val price = dataView.priceFrame(windowSize = 30)
    //val logPrice = price.mapValues(math.log)

    val prediction = query.tickers
      .filter(ticker => model.contains(ticker))
      .map { ticker => {
        val p = predictOne2(
          model(ticker),
          price.firstCol(ticker))
        (ticker, p)
      }}

    Prediction(HashMap[String, Double](prediction:_*))
  }
}
