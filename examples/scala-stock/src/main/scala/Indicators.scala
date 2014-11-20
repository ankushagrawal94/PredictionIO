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

abstract class BaseIndicator extends Serializable {
	def getTraining(input: Series[DateTime, Double]): Series[DateTime, Double]
	def getOne(input: Series[DateTime, Double]): Double
}


/* Example indicator */
class RSIIndicator(period: Int) extends BaseIndicator {
	private def calcRS(shiftedInput: Series[DateTime, Double], period: Int): Series[DateTime, Double] = {
		val positiveValues = shiftedInput.mapValues[Double]( (x: Double) => if (x > 0) x else 0 )
		val negativeValues = shiftedInput.mapValues[Double]( (x: Double) => if (x < 0) x else 0 )
		val avgPosVals = positiveValues.rolling[Double](period, (f: Series[DateTime, Double]) => f.mean)
		val avgNegVals = negativeValues.rolling[Double](period, (f: Series[DateTime, Double]) => f.mean)
		avgPosVals/avgNegVals
	}
	
	def getTraining(input: Series[DateTime, Double]): Series[DateTime, Double] = {
		val shifted = (input - input.shift(period)).fillNA(_ => 0.0)
		calcRS(shifted, period).mapValues[Double]( (x: Double) => 100 - (100/(1 + x)) ).fillNA(_  => 0.0)
	}

	def getOne(input: Series[DateTime, Double]): Double = {
		0.0
	}

}



	//private def getRet(logPrice: Frame[DateTime, String, Double], period: Int) =
	//	(logPrice - logPrice.shift(period)).mapVec[Double](_.fillNA(_ => 0.0))

	/*  RSI
	 *  Input: logPrice: Frame[DateTime, String, Double], d: Int
	 *  Return: Same frame
	 */
	//def calcRSI(logPrice: Frame[DateTime, String, Double], returnPeriod: Int) = {
	//	val rsFrame = calcRS(getRet(logPrice, returnPeriod), 14)
	//	val rsiFrame = rsFrame.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))
	//
	//	// Fill in first 14 days offset with zero
  //	val rsi = rsiFrame.reindexRow(logPrice.rowIx)
  //	rsi.mapVec[Double](_.fillNA(_  => 0.0))
	//
	//}

	/*
	* @authors - Matt & Leta
	* Tested the following functions using simple calls in
	* the interpreter. Need to find way to test on the whole.
	*/
	//private def calcRS(logPrice: Frame[DateTime, String, Double], period: Int) = {
	//	//(logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))
	//
	//	//Positive Vecs
	//	val posFrame = logPrice.mapValues[Double]( (x:Double) => if (x > 0) x else 0)
	//	println("calcRS: Found positive vecs")
	//
	//	//Negative Vecs
	//	val negFrame = logPrice.mapValues[Double]( (x:Double) => if (x < 0) x else 0)
	//	println("calcRS: Found negative vecs")
	//
	//	//Get the sum of positive Frame
	//	val avgPosFrame = posFrame.rolling[Double] (period, (f: Series[DateTime,Double]) => f.mean)
	//	println("calcRS: Found sum of positive frames")
	//
	//	//Get sum of negative
	//	val avgNegFrame = negFrame.rolling[Double] (period, (f: Series[DateTime,Double]) => f.mean)
	//	println("calcRS: Found sum of negative frames")
	//
	//	val rsFrame = avgPosFrame/avgNegFrame
	//	println("calcRS: Found rsFrame")
	//
	//	println("calcRS: Returning from calcRS")
	//
	//	rsFrame
	//}

//}
