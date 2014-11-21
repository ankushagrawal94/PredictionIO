package io.prediction.examples.stock

import org.saddle._
import org.saddle.index.IndexTime

import scala.collection.immutable.HashMap
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import com.github.nscala_time.time.Imports._

import scala.math

import math._

import nak.regress.LinearRegression

abstract class BaseIndicator {
	def getTraining(logPrice: Series[DateTime, Double], returnPeriod: Int): Series[DateTime, Double]
}

class RSIIndicator extends BaseIndicator {

	//private def getRet(logPrice: Frame[DateTime, String, Double], period: Int) =
	//	(logPrice - logPrice.shift(period)).mapVec[Double](_.fillNA(_ => 0.0))

	private def getRet(logPrice: Series[DateTime, Double], period: Int) =
		(logPrice - logPrice.shift(period)).fillNA(_ => 0.0)

	/*  RSI
	 *  Input: logPrice: Frame[DateTime, String, Double], d: Int
	 *  Return: Same frame
	 */
	//def getTraining(logPrice: Frame[DateTime, String, Double], returnPeriod: Int) = {
	//	val rsFrame = calcRS(getRet(logPrice, returnPeriod), 14)
	//	val rsiFrame = rsFrame.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))
	//
	//	// Fill in first 14 days offset with zero
  //	val rsi = rsiFrame.reindexRow(logPrice.rowIx)
  //	rsi.mapVec[Double](_.fillNA(_  => 0.0))
	//}

	def getTraining(logPrice: Series[DateTime, Double], returnPeriod: Int): Series[DateTime, Double] = {
		val rsSeries = calcRS(getRet(logPrice, returnPeriod), 14)
		val rsiSeries = rsSeries.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))

		// Fill in first 14 days offset with zero
		rsiSeries.reindex(logPrice.rowIx).fillNA(_  => 0.0)
	}

	/*
	* @authors - Matt & Leta
	* Tested the following functions using simple calls in
	* the interpreter. Need to find way to test on the whole.
	*/
	private def calcRS(logPrice: Series[DateTime, Double], period: Int): Series[DateTime, Double] = {
		//(logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))

		//Positive Vecs
		val posSeries = logPrice.mapValues[Double]( (x:Double) => if (x > 0) x else 0)
		println("calcRS: Found positive vecs")

		//Negative Vecs
		val negSeries = logPrice.mapValues[Double]( (x:Double) => if (x < 0) x else 0)
		println("calcRS: Found negative vecs")

		//Get the sum of positive Frame
		val avgPosSeries = posSeries.rolling[Double] (period, (f: Series[DateTime,Double]) => f.mean)
		println("calcRS: Found sum of positive frames")

		//Get sum of negative
		val avgNegSeries = negSeries.rolling[Double] (period, (f: Series[DateTime,Double]) => f.mean)
		println("calcRS: Found sum of negative frames")

		val rsSeries = avgPosSeries/avgNegSeries
		println("calcRS: Found rsFrame")

		println("calcRS: Returning from calcRS")

		rsSeries
	}

}
