module com.zedo.bangusearcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.zedo.fxcomponent;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.core5.httpcore5.h2;
    requires com.alibaba.fastjson2;

    opens com.zedo.bangumisearcher to javafx.fxml;
    exports com.zedo.bangumisearcher;
}