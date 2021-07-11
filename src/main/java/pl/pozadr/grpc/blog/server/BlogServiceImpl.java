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
        Document result = getDocument(responseObserver, blogId);

        System.out.println("Blog found, sending response.");
        var blog = documentToBlog(result);

        var response = ReadBlogResponse.newBuilder()
                .setBlog(blog)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Received Update Blog request");
        var requestBlog = request.getBlog();
        var requestBlogId = requestBlog.getId();

        System.out.println("Searching for a Blog to update");
        var documentToUpdate = getDocument(responseObserver, requestBlogId);

        System.out.println("Blog found, updating Blog with id: " + requestBlogId);
        var replacement = new Document("_id", new ObjectId(requestBlogId))
                .append("author_id", requestBlog.getAuthorId())
                .append("title", requestBlog.getTitle())
                .append("content", requestBlog.getContent());

        collection.replaceOne(eq("_id", documentToUpdate.getObjectId("_id")), replacement);

        var responseBlog = documentToBlog(replacement);
        var response = UpdateBlogResponse.newBuilder()
                .setBlog(responseBlog)
                .build();

        System.out.println("Blog updated. Sending as a response.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Document getDocument(StreamObserver responseObserver, String id) {
        System.out.println("Searching for a Blog to update");
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(id)))
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
            return new Document();
        } else {
            return result;
        }
    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setId(document.getObjectId("_id").toString())
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .build();
    }
}
