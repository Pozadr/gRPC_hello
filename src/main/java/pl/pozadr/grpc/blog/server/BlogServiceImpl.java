package pl.pozadr.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;
import pl.pozadr.blog.*;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase database = mongoClient.getDatabase("mydb");
    private final MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received Create Blog request");
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

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received Read Blog request");
        var blogId = request.getBlogId();

        System.out.println("Searching for a Blog");
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (IllegalArgumentException ex) {
            System.out.println("Blog Not found");
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("The blog with corresponding id was not found")
                            .augmentDescription(ex.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            System.out.println("Blog Not found");
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("The blog with corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog found, sending response.");
            var blog = Blog.newBuilder()
                    .setId(result.get("_id").toString())
                    .setAuthorId(result.getString("author_id"))
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content"))
                    .build();

            var response = ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
