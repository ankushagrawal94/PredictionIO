# Documentation

Please refer to http://docs.prediction.io/

Meet The UCSD Team:

Ankush Agrawal
Leta He
Gaurang Patel
Matthew Schwegler

Jekyll source code under docs/manual
-------------------------------------------

Start-up Instructions:

1. First get your Pandas
Where: PredictionIO-Python-SDK
Run: sudo pip install pandas==0.13.1 
pip command not found? install python from curl -O https://bootstrap.pypa.io/get-pip.py
python get-pip.py
and then run sudo pip install pandas

2. Edit import_yahoo.py
Where: PredictionIO-Python-SDK/examples/import_yahoo.py
At the end of file, find the following:
if name == 'main':
  #import all(app_id=?)
  import data with gaps(app_id=1)
  #import one(app_id=1)
And, uncomment the first import, replacing app id with your own id. Next, comment the second import statement (import data with gaps).

3. Now, import Yahoo Finance data.
Where: PredictionIO-Python-SDK/examples
Run: sudo python -m examples.import_yahoo

4. Start a spark master and slave (worker)
Where: within the spark directory (in our version: spark-1.1.0-bin-hadoop2.4)
Run:    1. sbin/start-master.sh 
		2. sbin/start-slave.sh
				OR
   		sbin/start-all.sh

5. Start Hbase
Where: hbase source folder (our version: hbase-0.98.6-hadoop2)
Run: bin/start-hbase.sh

6. Start ElasticSearch
Where: elasticsearch source folder (our version: elasticsearch-1.3.4)
Run: bin/elasticsearch

7. Configure PredictionIO template
Where: PredictionIO/conf
Run: - mv pio.env.sh.template to .template pio.env.sh
Edit pio.env.sh, change SPARK_HOME=<current address of spark directory>

8. Now make the distribution of PredictionIO
Where: cloned PredictionIO directory (with source code, make sure code is updated, git pull)
Run: ./make-distribution.sh

9. Ensure all dependencies are working
Type jps into console should see 

10. Check localhost8080
Navigate to http:localhost8080
Should see a master address and worker node

11. Edit scala-stock
go to examples/scala-stock/src/main/scala###
Edit YahooDataSource.scala
Go to end of file to PredefinedDSP function
Edit app_id to match the one from step 2

12. Run scala-stock
Go to PredictionIO/examples/scala-stock
Now type: ../../bin/pio run --asm io.prediction.examples.stock.YahooDataSourceRun -- --master <Your spark master address found at http:local8080> --driver-memory <4-12G>

13. Open dashboard to view the results
In PredictionIO folder
Type /bin/pio dashboard
go to url: http:localhost900 to view output
