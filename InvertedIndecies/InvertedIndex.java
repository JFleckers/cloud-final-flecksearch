import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class InvertedIndex {

    public static class IndexMapper extends Mapper<Object, Text, Text, Text> {

        ArrayList<String> stopwords = new ArrayList<String>(Arrays.asList("THE", "AND", "OF", "TO", "A", "IN", "HE", "THAT", "I", "HIS"));

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            FileSplit fs = (FileSplit) context.getInputSplit();
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            String[] line = value.toString().split("\\s");
            //TODO implement stopwords
            for(String s:line){
                s = s.toUpperCase();
                s = s.replaceAll("\\p{Punct}","");
                s= s.replaceAll("\\s", "");
                if(s.length() == 0){
                    continue;
                }
                if(stopwords.contains(s)){
                    continue;
                }
                Integer temp = hm.get(s);
                if(temp != null){
                    hm.replace(s,temp+1);
                }
                else{
                    hm.put(s,1);
                }
            }
            for(Map.Entry<String, Integer> e :hm.entrySet()){
                context.write(new Text(e.getKey()),new Text(fs.getPath().getParent().getName()
                        +"/"+fs.getPath().getName()+":"+e.getValue()));
            }
        }
    }

    public static class IndexReducer extends Reducer<Text,Text,Text,Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            //Use a hashmap to map each document to its total occurrences of the term
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            for(Text t: values){
                String doc = t.toString().split(":")[0];
                Integer count = Integer.parseInt(t.toString().split(":")[1]);
                Integer temp = hm.get(doc);
                if(temp != null){
                    hm.replace(doc,count+temp);
                }
                else {
                    hm.put(doc,count);
                }
            }
            String base = "";
            for(Map.Entry<String, Integer> e :hm.entrySet()){
                base = base.concat(e.getKey()+":"+e.getValue()+";");
            }
            context.write(key,new Text(base));
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "inverted index");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(IndexMapper.class);
        job.setReducerClass(IndexReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}