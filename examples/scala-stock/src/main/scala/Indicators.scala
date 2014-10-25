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
		val upday = getU(logPrice, d)
		val downday = getD(logPrice, d)
		100 - (100/1+CalculateRS(upday, downday, d))
	}

	// RS
	private def CalculateRS(/*input*/) {
		// EMA(U, D, k, d)/EMA(D, U, k, d)
	}

	// EMA(U, D, k, d), EMA(D, U, k, d)
	// iterates through frame
	private def iterateEMA(/*input*/) {

	}

	// EMA
	private def CalculateEMA(todayPrice: Double, yesterdayPrice: Double, d: Int) {
		val k = 2/(d + 1)
		todayPrice * k + yesterdayPrice * (1-k)
	}

	// upday closing gains
	private def getU(logPrice: Frame[DateTime, String, Double], d: Int) {

	}

	// downday closing losses
	private def getD(logPrice: Frame[DateTime, String, Double], d: Int) {

	}
}