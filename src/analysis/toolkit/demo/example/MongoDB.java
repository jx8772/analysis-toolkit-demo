package analysis.toolkit.demo.example;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for Test Java driver's connection 
 * with MongoDB
 * complete example can be found on http://www.mkyong.com/mongodb/java-mongodb-hello-world-example/
 * @version 1.0 06.28.2013
 * @author Xiang Ji
 */
public class MongoDB {
    public static void main(String[] args) {
        try {
            MongoClient mongo = new MongoClient( "localhost" , 27017 );
            
            List<String> dbs = mongo.getDatabaseNames();
            System.out.println("all the database names:");
            for(String db : dbs){
                System.out.println(db);
            }
            
            DB db = mongo.getDB("data");
            Set<String> tables = db.getCollectionNames();
            System.out.println("all the collection names in " + db.getName());
            for(String coll : tables){
                System.out.println(coll);
            }
            
//            DBCollection table = db.getCollection("user");
//            BasicDBObject document = new BasicDBObject();
//            document.put("name", "alex");
//            document.put("age", 21);
//            document.put("createdDate", new Date());
//            table.insert(document);
            
//            DBCollection table = db.getCollection("user");
//            BasicDBObject searchQuery = new BasicDBObject();
//            searchQuery.put("age", 21);
//            DBCursor cursor = table.find(searchQuery);
//            System.out.println("The results of the query are: ");
//            while (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            }
            
              //show all document the collection: boston
//              DBCollection table = db.getCollection("boston");
//              BasicDBObject searchQuery = new BasicDBObject();
//              DBCursor cursor = table.find();
//              System.out.println("The results of the query are: ");
//              while (cursor.hasNext()) {
//                  System.out.println(cursor.next());
//              } 
            
              //delete all document in the collection: boston
              
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(MongoDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
