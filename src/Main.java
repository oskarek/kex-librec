import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.cf.rating.SVDPlusPlusRecommender;
import net.librec.recommender.ext.SlopeOneRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

import java.util.ArrayList;
import java.util.List;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by oskarek on 2017-04-22.
 */
public class Main {
  public static void main(String[] args) throws Exception {
    // build data model
    Configuration conf = new Configuration();
    conf.set("data.input.path","movielens");
    conf.set("dfs.data.dir", "/Users/oskarek/Documents/Skola/Kex/librec/data");
    Configuration.Resource resource = new Configuration.Resource("rec/cf/userknn-test.properties");
    conf.addResource(resource);
    TextDataModel dataModel = new TextDataModel(conf);
    dataModel.buildDataModel();

    // build recommender context
    RecommenderContext context = new RecommenderContext(conf, dataModel);

    // build similarity
    conf.set("rec.recommender.similarity.key" ,"item");
    RecommenderSimilarity similarity = new PCCSimilarity();
    similarity.buildSimilarityMatrix(dataModel);
    context.setSimilarity(similarity);

    long startTime = System.nanoTime();

    // build recommender
    Recommender recommender = new UserKNNRecommender();
    recommender.setContext(context);



    // run recommender algorithm
    recommender.recommend(context);

    startTime = TimeUnit.NANOSECONDS.toMillis(startTime);

    System.out.println("Time elapsed: " + (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime));

    // evaluate the recommended result
    RecommenderEvaluator evaluator = new RMSEEvaluator();
    System.out.println("RMSE: " + recommender.evaluate(evaluator));

    MAEEvaluator maeEvaluator = new MAEEvaluator();
    System.out.println("MAE: " + recommender.evaluate(maeEvaluator));

    // set id list of filter
    List<String> userIdList = new ArrayList<>();
    List<String> itemIdList = new ArrayList<>();
    userIdList.add("1");
    itemIdList.add("70");

    // filter the recommended result
    List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
    GenericRecommendedFilter filter = new GenericRecommendedFilter();
    filter.setUserIdList(userIdList);
    filter.setItemIdList(itemIdList);
    recommendedItemList = filter.filter(recommendedItemList);

  }
}