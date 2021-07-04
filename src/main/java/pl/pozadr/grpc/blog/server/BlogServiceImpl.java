package pl.pozadr.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import pl.pozadr.blog.Blog;
import pl.pozadr.blog.BlogServiceGrpc;
import pl.pozadr.blog.CreateBlogRequest;
import pl.pozadr.blog.CreateBlogResponse;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase database = mongoClient.getDatabase("mydb");
    private final MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received CreateBlog request");
        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting blog...");
        collection.insertOne(doc);

        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: " + id);

        var response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder()
                        .setId(id)
                        .build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
