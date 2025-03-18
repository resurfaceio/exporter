// © 2016-2025 Graylog, Inc.

package io.resurface.exporter;

import io.resurface.ndjson.HttpMessage;
import io.resurface.ndjson.MessageFileWriter;

import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Exports NDJSON from Resurface database.
 */
public class Main {

    /**
     * Runs exporter as command-line program.
     */
    public static void main(String[] args) throws Exception {
        new Main();
    }

    /**
     * Connects to remote database and writes to local NDJSON file.
     */
    public Main() throws Exception {
        // read location of output file
        String file = System.getProperty("FILE");
        if (file == null) throw new IllegalArgumentException("Missing FILE");
        System.out.println("FILE=" + file);
        // todo add command-line option to overwrite?

        // create connection properties
        Properties properties = new Properties();
        String user = System.getProperty("USER");
        System.out.println("USER=" + user);
        properties.setProperty("user", user);
        String password = System.getProperty("PASSWORD");
        System.out.println("PASSWORD=" + password.replaceAll(".", "*"));
        properties.setProperty("password", password);

        // calculate destination url if not provided
        String url = System.getProperty("URL");
        if (url == null) {
            String host = System.getProperty("HOST");
            if (host == null) host = "localhost";
            System.out.println("HOST=" + host);
            String port = System.getProperty("PORT");
            if (port == null) port = "7701";
            System.out.println("PORT=" + port);
            url = "jdbc:trino://";
            if (port.equals("443")) {
                properties.setProperty("SSL", "true");
                properties.setProperty("SSLVerification", "NONE");
                url += host;
            } else if (port.equals("80")) {
                url += host;
            } else {
                url += host + ":" + port;
            }
        }
        System.out.println("URL=" + url);

        // read limit options
        long limit_messages = Long.parseLong(System.getProperty("LIMIT_MESSAGES", "0"));
        System.out.println("LIMIT_MESSAGES=" + limit_messages);
        long max_call_age_in_days = Long.parseLong(System.getProperty("MAX_CALL_AGE_IN_DAYS", "14"));
        System.out.println("MAX_CALL_AGE_IN_DAYS=" + max_call_age_in_days);

        // build sql statement
        String sql = UNLOAD_SQL.replace("!MAX_CALL_AGE_IN_DAYS!", String.valueOf(max_call_age_in_days));
        if (limit_messages > 0) sql += "\nlimit " + limit_messages;
        System.out.println("SQL=" + sql + "\n");

        // write all messages returned from database
        try (MessageFileWriter writer = new MessageFileWriter(file)) {
            Connection c = DriverManager.getConnection(url, properties);
            Statement s = c.createStatement();
            try (ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    HttpMessage m = new HttpMessage();
                    m.set_interval_millis(rs.getLong(1));
                    m.set_request_address(rs.getString(2));
                    m.set_request_body(rs.getString(3));
                    m.set_request_content_type(rs.getString(4));
                    m.set_request_headers_json(rs.getString(5));
                    m.set_request_method(rs.getString(6));
                    m.set_request_params_json(rs.getString(7));
                    m.set_request_url(rs.getString(8));
                    m.set_request_user_agent(rs.getString(9));
                    m.set_response_body(rs.getString(10));
                    m.set_response_code(rs.getString(11));
                    m.set_response_content_type(rs.getString(12));
                    m.set_response_headers_json(rs.getString(13));
                    m.set_response_time_millis(rs.getLong(14));
                    writer.write(m);
                    messages++;
                    if (messages % 5000 == 0) status();
                }
            }
        }

        // print final status
        status();
    }

    /**
     * Print status summary.
     */
    private void status() {
        long elapsed = System.currentTimeMillis() - started;
        long rate = (messages * 1000 / elapsed);
        System.out.println("Messages: " + messages + ", Elapsed time: " + elapsed + " ms, Rate: " + rate + " msg/sec");
    }

    private long messages = 0;
    private final long started = System.currentTimeMillis();

    private static final String UNLOAD_SQL = """
            select
            interval_millis,
            request_address,
            request_body,
            request_content_type,
            request_headers,
            request_method,
            request_params,
            request_url,
            request_user_agent,
            response_body,
            response_code,
            response_content_type,
            response_headers,
            response_time_millis
            from resurface.data.message
            where cast(from_unixtime(response_time_millis / 1000) as timestamp(6) with time zone) > (current_date - interval '!MAX_CALL_AGE_IN_DAYS!' day)""";

}