package io.prediction.examples.stock

import org.saddle._
import org.saddle.index.IndexTime

import scala.collection.immutable.HashMap
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import com.github.nscala_time.time.Imports._

import scala.math

import nak.regress.LinearRegression

class Indicators {

	/* RSI
	   Input: logPrice: Frame[DateTime, String, Double], d: Int
	   Return: Same frame

	 */
	def CalculateRSI(logPrice: Frame[DateTime, String, Double], d: Int) {
		100 - (100/1+CalculateRS(logPrice))
	}

	// RS
	private def CalculateRS(/*input*/) {

	}

	// EMA
	def CalculateEMA(/*input: today's price, yesterday's price*/) {
		val k = 2/(d + 1)
		// today's price * k + yesterday's price * (1-k)
	}

	// helper for EMA

	// Average
}