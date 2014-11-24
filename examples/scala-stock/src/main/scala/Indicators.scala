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

/*
 * Base class for indicators. All indicators should be defined as classes that extend
 * this base class. See RSIIndicator as an example. These indicators can then be
 * instantiated and passed into RegressionStrategy class.
 */
abstract class BaseIndicator extends Serializable {
	def getTraining(logPrice: Series[DateTime, Double]): Series[DateTime, Double]
	def getOne(input: Series[DateTime, Double]): Double
	def minWindowSize(): Int
}

class RSIIndicator(onCloseWindowSize: Int = 14, period: Int) extends BaseIndicator {

	private def getRet(logPrice: Series[DateTime, Double]) =
		(logPrice - logPrice.shift(period)).fillNA(_ => 0.0)

	def minWindowSize(): Int = onCloseWindowSize
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

	def getTraining(logPrice: Series[DateTime, Double]): Series[DateTime, Double] = {
		val rsSeries = calcRS(getRet(logPrice), 14)
		val rsiSeries = rsSeries.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))

		// Fill in first 14 days offset with zero
		rsiSeries.reindex(logPrice.rowIx).fillNA(_  => 0.0)
	}

	def getOne(logPrice: Series[DateTime, Double]): Double = {
		//Series[DateTime, Double] sliced = logPrice.slice(logPrice.length-1-onCloseWindowSize, logPrice.length)
		//val rsSeries= calcRS(getRet(sliced), 14)
		//val rsiSeries = rsSeries.mapValues[Double] ((x: Double) => 100 - (100/(1 + x)))
		//rsiSeries.last

		val rsSeries = calcRS(getRet(logPrice), 14)
		val rsiSeries = rsSeries.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))
		rsiSeries.last
	}
}

class ShiftsIndicator(onCloseWindowSize: Int = 14, period: Int) extends BaseIndicator {

	private def getRet(logPrice: Series[DateTime, Double], frameShift:Int = period) =
		(logPrice - logPrice.shift(frameShift)).fillNA(_ => 0.0)

	def minWindowSize(): Int = onCloseWindowSize

	def getTraining(logPrice: Series[DateTime, Double]): Series[DateTime, Double] = {
		getRet(logPrice)
	}

	def getOne(input: Series[DateTime, Double]): Double = {
		getRet(input).last
	}
}
