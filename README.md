**添加hbase-site.xml：**

```
<property>
                <name>hbase.coprocessor.master.classes</name>
                <value>com.denovo.hbaseObserver.Hbase2EsObserver</value>
</property>
```



**hdfs一份jar包：**

```
hdfs dfs -put ~/IdeaProjects/Hbase_ES/target/Hbase_ES-1.0-SNAPSHOT-jar-with-dependencies.jar /user/user/
```

**Hbase安装目录下lib中一份：**

```
cp ~/IdeaProjects/Hbase_ES/target/Hbase_ES-1.0-SNAPSHOT-jar-with-dependencies.jar ~/apps/hbase-1.2.6/lib/
```



**例子：**

```
create 'Student','Sno','Sname','Ssex','Sage'

disable 'Student'
```

这里注意中间的"|"，后面逗号分割的是参数

```
alter 'Student' , METHOD =>'table_att','coprocessor'=>'hdfs:///user/user/Hbase_ES-1.0-SNAPSHOT-jar-with-dependencies.jar|com.denovo.hbaseObserver.Hbase2EsObserver|1001|cluster.name=elasticsearch,indexType=type,indexName=index,es_port=9300,es_host=localhost'

enable 'Student'
```



**数据例子：**

```
put 'Student','student1','Sno','1'
put 'Student','student1','Sname','s1'
put 'Student','student1','Ssex','male'
put 'Student','student1','Sage','68'
put 'Student','student2','Sno','2'
put 'Student','student2','Sname','s2'
put 'Student','student2','Ssex','male'
put 'Student','student2','Sage','69'
put 'Student','student3','Sno','3'
put 'Student','student3','Sname','s3'
put 'Student','student3','Ssex','male'
put 'Student','student3','Sage','65'
put 'Student','student4','Sno','4'
put 'Student','student4','Sname','s4'
put 'Student','student4','Ssex','male'
put 'Student','student4','Sage','1'
put 'Student','student5','Sno','5'
put 'Student','student5','Sname','s5'
put 'Student','student5','Ssex','male'
put 'Student','student5','Sage','66'
```

