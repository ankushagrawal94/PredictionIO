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

class Indicators {

	/*  RSI
	 *  Input: logPrice: Frame[DateTime, String, Double], d: Int
	 *  Return: Same frame
	 */
	def calcRSI(logPrice: Frame[DateTime, String, Double], d: Int) = {
		val rsFrame = calcRS(logPrice, d)
		rsFrame.mapValues[Double]( (x:Double) => 100 - (100/(1 + x)))
	}

	/*
	* @authors - Matt & Leta
	*Tested the following functions using simple calles in
	*the interpreter. need to find way to test on the whole.
	*/
	private def calcRS(logPrice: Frame[DateTime, String, Double], period: Int) = {
		//(logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))

		//Positive Vecs
		val posFrame = logPrice.mapValues[Double]( (x:Double) => if (x > 0) x else 0)
		println("calcRS: Found positive vecs")

		//Negative Vecs
		val negFrame = logPrice.mapValues[Double]( (x:Double) => if (x < 0) x else 0)
		println("calcRS: Found negative vecs")

		//Get the sum of positive Framse
		val sumPosFrame = posFrame.rolling[Double] (14, (f: Series[DateTime,Double]) => f.mean  )
		println("calcRS: Found sum of positive frames")

		//Get sum of negative
		val sumNegFrame = negFrame.rolling[Double] (14, (f: Series[DateTime,Double]) => f.mean  )
		println("calcRS: Found sum of negative frames")

		val rsFrame = sumPosFrame/sumNegFrame
		println("calcRS: Found rsFrame")

		println("calcRS: Returning from calcRS")

		rsFrame
	}
	
}
