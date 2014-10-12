#!/bin/bash
echo Starting H-Base
/Development/predictionIO/hbase-0.98.6-hadoop2/bin/start-hbase.sh
echo Started H-Base
sleep 1
echo Starting Master
/Development/predictionIO/spark-1.1.0-bin-hadoop2.4/sbin/start-master.sh
echo Started Master
sleep 1
echo Starting Slaves    
/Development/predictionIO/spark-1.1.0-bin-hadoop2.4/sbin/start-slaves.sh  spark://Ankushs-MacBook-Pro.local:7077
echo Started Slaves
sleep 1
cd /Development/predictionIO/PredictionIO/examples/scala-stock
export SPARK_HOME=/Development/predictionIO/spark-1.1.0-bin-hadoop2.4/
echo Running YahooDataSource
../../bin/pio run --asm io.prediction.examples.stock.YahooDataSourceRun -- --master spark://Ankushs-MacBook-Pro.local:7077 --driver-memory 7G
echo Ran YahooDataSource
sleep 10
echo Starting Dashboard
echo If you run into errors, edit the port number in the script
/Development/predictionIO/PredictionIO/bin/pio dashboard --port 9001
echo Started Dashboard. Check it out at: localhost:9001
