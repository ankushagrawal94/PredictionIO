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
<<<<<<< HEAD
		//100 - (100/1+CalculateRS(logPrice))
		//val upday = getU(logPrice, d)
		//val downday = getD(logPrice, d)
		//100 - (100/1+CalculateRS(upday, downday, d))
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
	def CalculateEMA(/*input: today's price, yesterday's price*/) {
		//val k = 2/(d + 1)
		// today's price * k + yesterday's price * (1-k)

	}

	// helper for EMA
	def getU(logPrice: Frame[DateTime, String, Double], d: Int) {
		(logPrice - logPrice.shift(d)).mapVec[Double](_.fillNA(_ => 0.0))
	}
	// Average


	def firstAvgGain(priceDelta: Frame[DateTime, String, Double], period: Int) {
		//paramater should be the change in price over the period
		//For period = 1day use ret1d = getRet(logPrice, 1)

		val numPeriods = 14
		var firstGains = new Array[Double](14)
		var tickerCount = 0
		for (ticker <- 0 to 100/*priceDelta(0,*).length*/) { //need someway of accessing every ticker in the map
			var numGains: Double = 0.0
			var average: Double = 0.0
			var day: Double = 0.0
			for (day <- 0 to (numPeriods-1) * period) {
				//println("day is: " + day.toString)
				var change = priceDelta.raw(ticker,day)
				if (change > 0) {
					numGains = numGains + 1
					average = average + change
				}
			}
			average = average / numGains
			tickerCount = tickerCount + 1
			firstGains(tickerCount) = average
		}
		return firstGains
	}

	def avgGain(priceDelta: Frame[DateTime, String, Double], d: Int) {

		//get the firstAvgGain
		//replace the data at the appropriate day (proabbly 15th day) in our map with the firstAvgGain data
		//iterate through the data starting at day = 15
		//multiply the previous day's gain by 13, add current day's gain
		//save this value to the current day

		//return the map
	}
}
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
