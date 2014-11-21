package io.prediction.examples.stock

import io.prediction.controller.Params

import org.saddle._
import org.saddle.index.IndexTime

import scala.collection.immutable.HashMap
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import com.github.nscala_time.time.Imports._

import scala.math

import nak.regress.LinearRegression

case class RegressionStrategyParams (
  indicators: Seq[(String, BaseIndicator)],  // (indicKey, indicator)
  maxTrainingWindowSize: Int
) extends Params

class RegressionStrategy (params: RegressionStrategyParams) extends StockStrategy[Map[String, DenseVector[Double]]] {
  val shifts = Seq(0, 1, 5, 22) // days used in regression model*/

  private def getRet(logPrice: Frame[DateTime, String, Double], d: Int) =
    (logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))

  /* Regress on a particular ticker */
  private def regress(
    calculatedData: Seq[Series[DateTime, Double]],
    retF1d: Series[DateTime, Double]) = {
    val array = (
      calculatedData.map(_.toVec.contents).reduce(_++_) ++
      Array.fill(retF1d.length)(1.0)).toArray[Double]
    val target = DenseVector[Double](retF1d.toVec.contents)
    val m = DenseMatrix.create[Double](retF1d.length, calculatedData.length + 1, array)
    val result = LinearRegression.regress(m, target)
    result
  }

  /* Apply indicators to a particular ticker series */
  private def getIndicSeq(logPrice: Series[DateTime, Double]): Seq[Series[DateTime, Double]] = {
    var retSeq = Seq[Series[DateTime, Double]]()
    var x = 0
    for (x <- 0 to params.indicators.length - 1) {
      retSeq = retSeq ++ Seq(params.indicators(x)._2.getTraining(logPrice))
    }
    retSeq
  }

  /* Helper for train */
  def createModel(dataView: DataView): Map[String, DenseVector[Double]] = {
    // trainingWindowSize - data time range
    val price = dataView.priceFrame(params.maxTrainingWindowSize) // map from ticker to array of stock prices
    val logPrice = price.mapValues(math.log)
    val active = dataView.activeFrame(params.maxTrainingWindowSize)


    //Calculate target data returns
    val retF1d = getRet(logPrice, -1)

    val timeIndex = price.rowIx // Get row indices
    val firstIdx = shifts.max + 3
    val lastIdx = timeIndex.length

    // Get array of all ticker strings
    val tickers = price.colIx.toVec.contents

    // For each active ticker, pass in an indicated series into regress
    val tickerModelMap = tickers
    .filter(ticker => (active.firstCol(ticker).findOne(_ == false) == -1))
    .map(ticker => {
      val model = regress(
        // Only pass in relevant data
        getIndicSeq(price.firstCol(ticker)).map(_.slice(firstIdx, lastIdx)),
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
      densVecArray = densVecArray ++ Array[Double](sp(shifts(0)) - sp(shifts(x)))
    }

    densVecArray = densVecArray ++ Array[Double](1)
    val vec = DenseVector[Double](densVecArray)

    //val vec = DenseVector[Double](
    //  sp(shifts(0)) - sp(shifts(1)),
    //  sp(shifts(0)) - sp(shifts(2)),
    //  sp(shifts(0)) - sp(shifts(3)),
    //  1)

    val p = coef.dot(vec)
    return p
  }

  /* Helper for predict */
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
