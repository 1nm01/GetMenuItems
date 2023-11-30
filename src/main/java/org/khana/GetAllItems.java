package org.khana;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import org.khana.entity.Menu;
import org.khana.response.ApiResponse;

import java.util.*;

public class GetAllItems implements RequestHandler<APIGatewayProxyRequestEvent, ApiResponse> {

    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    private static final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client);
    @Override
    public ApiResponse handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        Map<String, String> queryStringParameters = apiGatewayProxyRequestEvent.getQueryStringParameters();
        String restId = "";
        if(queryStringParameters.containsKey("restaurantId")){
            restId = queryStringParameters.get("restaurantId");
        }
        if(restId.isEmpty()){
            return failure();
        }
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(restId));

        DynamoDBQueryExpression<Menu> queryExpression = new DynamoDBQueryExpression<Menu>()
                .withKeyConditionExpression("RestaurantId = :v1")
                .withExpressionAttributeValues(eav);
        PaginatedQueryList<Menu> query = dynamoDBMapper.query(Menu.class, queryExpression);
//        For pagination
//        QueryResultPage<Menu> menuQueryResultPage = dynamoDBMapper.queryPage(Menu.class, queryExpression);
        Iterator<Menu> iterator = query.stream().iterator();
        List<Menu> allItems= new ArrayList<>();
        while(iterator.hasNext()){
            allItems.add(iterator.next());
        }
        Map<String,String> headers = Map.of("content-type", "application/json");
        Gson gson = new Gson();
        return new ApiResponse(200, headers, gson.toJson(allItems));
    }

    private ApiResponse failure(){
        Map<String,String> headers = new HashMap<>();
        return new ApiResponse(400, headers, "{\"message\" : \"Restaurant Invalid\"}");
    }
}
