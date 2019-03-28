package service.util;

/**
*
* @author umansilla
*/
public class ModelHttp {



   private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
   private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
   private String restUri;
   private String requestMethod;
   private String connectTimeout;
   private String socketTimeout;
   private String tlsVersion;
   private String httpAuth;
   private String username;
   private String password;
   private String clientId;
   private String clientSecret;
   private String oauthUrl;
   private String oauthToken;
   private String customizedHeaders;
   private String contentType;
   private String payload;
   private String inputSchema;
   private String jsonSchemaInReturn;
   private String outputSchema;

   public ModelHttp() {
   }

   public ModelHttp(String restUri, String requestMethod, String connectTimeout, String socketTimeout, String tlsVersion, String httpAuth, String username, String password, String clientId, String clientSecret, String oauthUrl, String oauthToken, String customizedHeaders, String contentType, String payload, String inputSchema, String jsonSchemaInReturn, String outputSchema) {
       this.restUri = restUri;
       this.requestMethod = requestMethod;
       this.connectTimeout = connectTimeout;
       this.socketTimeout = socketTimeout;
       this.tlsVersion = tlsVersion;
       this.httpAuth = httpAuth;
       this.username = username;
       this.password = password;
       this.clientId = clientId;
       this.clientSecret = clientSecret;
       this.oauthUrl = oauthUrl;
       this.oauthToken = oauthToken;
       this.customizedHeaders = customizedHeaders;
       this.contentType = contentType;
       this.payload = payload;
       this.inputSchema = inputSchema;
       this.jsonSchemaInReturn = jsonSchemaInReturn;
       this.outputSchema = outputSchema;
   }

   
   public String getRestUri() {
       return restUri;
   }

   public void setRestUri(String restUri) {
       this.restUri = restUri;
   }

   public String getRequestMethod() {
       return requestMethod;
   }

   public void setRequestMethod(String requestMethod) {
       this.requestMethod = requestMethod;
   }

   public String getConnectTimeout() {
       return connectTimeout;
   }

   public void setConnectTimeout(String connectTimeout) {
       this.connectTimeout = connectTimeout;
   }

   public String getSocketTimeout() {
       return socketTimeout;
   }

   public void setSocketTimeout(String socketTimeout) {
       this.socketTimeout = socketTimeout;
   }

   public String getTlsVersion() {
       return tlsVersion;
   }

   public void setTlsVersion(String tlsVersion) {
       this.tlsVersion = tlsVersion;
   }

   public String getHttpAuth() {
       return httpAuth;
   }

   public void setHttpAuth(String httpAuth) {
       this.httpAuth = httpAuth;
   }

   public String getUsername() {
       return username;
   }

   public void setUsername(String username) {
       this.username = username;
   }

   public String getPassword() {
       return password;
   }

   public void setPassword(String password) {
       this.password = password;
   }

   public String getClientId() {
       return clientId;
   }

   public void setClientId(String clientId) {
       this.clientId = clientId;
   }

   public String getClientSecret() {
       return clientSecret;
   }

   public void setClientSecret(String clientSecret) {
       this.clientSecret = clientSecret;
   }

   public String getOauthUrl() {
       return oauthUrl;
   }

   public void setOauthUrl(String oauthUrl) {
       this.oauthUrl = oauthUrl;
   }

   public String getOauthToken() {
       return oauthToken;
   }

   public void setOauthToken(String oauthToken) {
       this.oauthToken = oauthToken;
   }

   public String getCustomizedHeaders() {
       return customizedHeaders;
   }

   public void setCustomizedHeaders(String customizedHeaders) {
       this.customizedHeaders = customizedHeaders;
   }

   public String getContentType() {
       return contentType;
   }

   public void setContentType(String contentType) {
       this.contentType = contentType;
   }

   public String getPayload() {
       return payload;
   }

   public void setPayload(String payload) {
       this.payload = payload;
   }

   public String getInputSchema() {
       return inputSchema;
   }

   public void setInputSchema(String inputSchema) {
       this.inputSchema = inputSchema;
   }

   public String getJsonSchemaInReturn() {
       return jsonSchemaInReturn;
   }

   public void setJsonSchemaInReturn(String jsonSchemaInReturn) {
       this.jsonSchemaInReturn = jsonSchemaInReturn;
   }

   public String getOutputSchema() {
       return outputSchema;
   }

   public void setOutputSchema(String outputSchema) {
       this.outputSchema = outputSchema;
   }
   
       /**
    * @return the DEFAULT_CONNECT_TIMEOUT
    */
   public static int getDEFAULT_CONNECT_TIMEOUT() {
       return DEFAULT_CONNECT_TIMEOUT;
   }

   /**
    * @return the DEFAULT_SOCKET_TIMEOUT
    */
   public static int getDEFAULT_SOCKET_TIMEOUT() {
       return DEFAULT_SOCKET_TIMEOUT;
   }

   @Override
   public String toString() {
       return "ModelHttp{" + "restUri=" + restUri + ", requestMethod=" + requestMethod + ", connectTimeout=" + connectTimeout + ", socketTimeout=" + socketTimeout + ", tlsVersion=" + tlsVersion + ", httpAuth=" + httpAuth + ", username=" + username + ", password=" + password + ", clientId=" + clientId + ", clientSecret=" + clientSecret + ", oauthUrl=" + oauthUrl + ", oauthToken=" + oauthToken + ", customizedHeaders=" + customizedHeaders + ", contentType=" + contentType + ", payload=" + payload + ", inputSchema=" + inputSchema + ", jsonSchemaInReturn=" + jsonSchemaInReturn + ", outputSchema=" + outputSchema + '}';
   }
   
   
   
}
