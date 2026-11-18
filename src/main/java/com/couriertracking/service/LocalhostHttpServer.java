package com.couriertracking.service;

import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.util.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded Zero-Dependency HTTP Web Server running on localhost:8080.
 * Uses Java SE standard com.sun.net.httpserver.HttpServer.
 * Provides a live browser tracking & dashboard portal synchronized with MySQL/JDBC.
 */
public class LocalhostHttpServer {
    private static final Logger LOGGER = Logger.getLogger(LocalhostHttpServer.class.getName());
    private static HttpServer server;
    private static int activePort = 8080;

    public static synchronized int startServer(ParcelService parcelService, TrackingService trackingService) {
        if (server != null) {
            return activePort;
        }

        int[] candidatePorts = {8080, 8081, 8888, 9090};
        for (int port : candidatePorts) {
            try {
                // Bind explicitly to 0.0.0.0 so localhost, 127.0.0.1, and hostname all resolve
                server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
                activePort = port;
                break;
            } catch (IOException e) {
                LOGGER.warning("Port " + port + " is in use, trying next...");
            }
        }

        if (server == null) {
            LOGGER.severe("Could not bind HTTP server to any candidate port.");
            return -1;
        }

        // Web Dashboard Handler
        server.createContext("/", new DashboardWebHandler(parcelService, trackingService));

        // REST API Handlers
        server.createContext("/api/stats", new StatsApiHandler(parcelService));
        server.createContext("/api/parcels", new ParcelsApiHandler(parcelService));
        server.createContext("/api/track", new TrackApiHandler(parcelService, trackingService));

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==========================================================");
        System.out.println(" 🌐 LOCALHOST WEB SERVER RUNNING AT: http://localhost:" + activePort);
        System.out.println(" Open http://localhost:" + activePort + " in your browser to view live web portal!");
        System.out.println("==========================================================");

        return activePort;
    }

    public static void main(String[] args) {
        com.couriertracking.util.DatabaseInitializer.initialize();
        ParcelService ps = new ParcelService();
        TrackingService ts = new TrackingService();
        int port = startServer(ps, ts);
        System.out.println("Server running on http://localhost:" + port + " ... Press Ctrl+C to stop.");
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static int getActivePort() {
        return activePort;
    }

    private static class DashboardWebHandler implements HttpHandler {
        private final ParcelService parcelService;
        private final TrackingService trackingService;

        DashboardWebHandler(ParcelService parcelService, TrackingService trackingService) {
            this.parcelService = parcelService;
            this.trackingService = trackingService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQuery(query);
                String cno = params.get("cno");

                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
                html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                html.append("<title>CRX Express - Courier Tracking Portal</title>");
                html.append("<style>");
                html.append(":root { --primary: #2563eb; --navy: #0f172a; --bg: #f8fafc; --card: #ffffff; --border: #e2e8f0; }");
                html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: var(--bg); margin: 0; color: #1e293b; }");
                html.append(".header { background: var(--navy); color: white; padding: 20px 32px; display: flex; justify-content: space-between; align-items: center; }");
                html.append(".header h1 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }");
                html.append(".badge-db { background: #10b981; color: white; font-size: 11px; padding: 4px 10px; border-radius: 12px; font-weight: 600; }");
                html.append(".container { max-width: 1180px; margin: 28px auto; padding: 0 20px; }");
                html.append(".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px; }");
                html.append(".card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 18px 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }");
                html.append(".card h3 { margin: 0 0 6px 0; font-size: 12px; text-transform: uppercase; color: #64748b; letter-spacing: 0.5px; }");
                html.append(".card .value { font-size: 28px; font-weight: 700; color: #0f172a; }");
                html.append(".search-box { display: flex; gap: 10px; margin-bottom: 24px; }");
                html.append(".search-box input { flex: 1; padding: 12px 16px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; outline: none; }");
                html.append(".search-box button { background: var(--primary); color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; }");
                html.append("table { width: 100%; border-collapse: collapse; text-align: left; }");
                html.append("th { background: #f8fafc; padding: 12px 14px; font-size: 12px; font-weight: 600; color: #64748b; border-bottom: 1px solid var(--border); }");
                html.append("td { padding: 12px 14px; font-size: 13px; border-bottom: 1px solid #f1f5f9; }");
                html.append(".status { padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 700; display: inline-block; }");
                html.append(".status-DELIVERED { background: #ecfdf5; color: #10b981; }");
                html.append(".status-IN_TRANSIT { background: #fef3c7; color: #d97706; }");
                html.append(".status-BOOKED { background: #eff6ff; color: #3b82f6; }");
                html.append(".status-OUT_FOR_DELIVERY { background: #f5f3ff; color: #8b5cf6; }");
                html.append(".timeline-item { padding: 10px 0; border-left: 2px solid var(--primary); margin-left: 10px; padding-left: 16px; position: relative; }");
                html.append(".timeline-item::before { content: ''; position: absolute; left: -6px; top: 14px; width: 10px; height: 10px; border-radius: 50%; background: var(--primary); }");
                html.append("</style></head><body>");

                // Header
                html.append("<div class='header'>");
                html.append("<div><h1>🚚 CRX EXPRESS LOGISTICS</h1><div style='font-size:12px; opacity:0.8;'>Connected to Localhost Database Engine</div></div>");
                html.append("<div><span class='badge-db'>● Engine: ").append(DatabaseConnection.getInstance().getActiveDatabaseType()).append("</span></div>");
                html.append("</div>");

                html.append("<div class='container'>");

                // If user searched for a consignment
                if (cno != null && !cno.trim().isEmpty()) {
                    Optional<Parcel> parcelOpt = parcelService.getParcelByConsignmentNo(cno.trim());
                    if (parcelOpt.isPresent()) {
                        Parcel p = parcelOpt.get();
                        List<TrackingHistory> history = trackingService.getTrackingTimeline(p.getConsignmentNo());

                        html.append("<div class='card' style='margin-bottom: 24px; border-left: 4px solid var(--primary);'>");
                        html.append("<div style='display:flex; justify-content:space-between; align-items:center;'>");
                        html.append("<h2 style='margin:0;'>Consignment: ").append(p.getConsignmentNo()).append("</h2>");
                        html.append("<span class='status status-").append(p.getStatus().name()).append("'>").append(p.getStatus().getDisplayName()).append("</span>");
                        html.append("</div>");
                        html.append("<p style='margin: 8px 0; color:#64748b;'>Route: <b>").append(p.getSourceBranchName()).append("</b> → <b>").append(p.getDestinationBranchName()).append("</b> | Weight: ").append(String.format("%.2f kg", p.getWeight())).append(" | Charge: ").append(p.getFormattedCharge()).append("</p>");
                        html.append("<p style='margin: 0; font-size:12px;'>Sender: ").append(p.getSenderName()).append(" | Receiver: ").append(p.getReceiverName()).append(" (").append(p.getReceiverPhone()).append(")</p>");

                        html.append("<h3 style='margin-top:20px;'>Tracking Timeline</h3>");
                        for (TrackingHistory th : history) {
                            html.append("<div class='timeline-item'>");
                            html.append("<div style='font-weight:600; font-size:13px;'>").append(th.getStatus().getDisplayName()).append(" &bull; <span style='color:#64748b;'>").append(th.getLocation()).append("</span></div>");
                            html.append("<div style='font-size:12px; color:#475569;'>").append(th.getRemarks()).append("</div>");
                            html.append("<div style='font-size:11px; color:#94a3b8;'>").append(th.getFormattedDate()).append("</div>");
                            html.append("</div>");
                        }
                        html.append("<div style='margin-top:16px;'><a href='/' style='color:var(--primary); text-decoration:none; font-weight:600;'>&larr; Back to Dashboard</a></div>");
                        html.append("</div>");
                    } else {
                        html.append("<div class='card' style='margin-bottom:24px; background:#fef2f2; border-color:#fca5a5; color:#991b1b;'>");
                        html.append("Consignment <b>").append(cno).append("</b> not found. <a href='/' style='color:#991b1b; font-weight:bold;'>View all shipments</a>");
                        html.append("</div>");
                    }
                }

                // Stats Cards
                DashboardStats stats = parcelService.getDashboardStatistics();
                html.append("<div class='stats-grid'>");
                html.append("<div class='card'><h3>Total Parcels</h3><div class='value'>").append(stats.getTotalParcels()).append("</div></div>");
                html.append("<div class='card'><h3>Booked</h3><div class='value' style='color:#3b82f6;'>").append(stats.getBookedParcels()).append("</div></div>");
                html.append("<div class='card'><h3>In Transit</h3><div class='value' style='color:#d97706;'>").append(stats.getInTransitParcels()).append("</div></div>");
                html.append("<div class='card'><h3>Out for Delivery</h3><div class='value' style='color:#8b5cf6;'>").append(stats.getOutForDeliveryParcels()).append("</div></div>");
                html.append("<div class='card'><h3>Delivered</h3><div class='value' style='color:#10b981;'>").append(stats.getDeliveredParcels()).append("</div></div>");
                html.append("<div class='card'><h3>Total Revenue</h3><div class='value' style='color:#0f172a;'>₹ ").append(String.format("%.2f", stats.getTotalRevenue())).append("</div></div>");
                html.append("</div>");

                // Search Bar
                html.append("<form method='GET' action='/' class='search-box'>");
                html.append("<input type='text' name='cno' placeholder='Track Consignment Number (e.g. CRX202611180001)...' required />");
                html.append("<button type='submit'>Track Package</button>");
                html.append("</form>");

                // All Parcels Table
                List<Parcel> parcels = parcelService.getAllParcels();
                html.append("<div class='card'>");
                html.append("<h2 style='margin: 0 0 16px 0; font-size: 16px;'>Active Logistics Directory</h2>");
                html.append("<table>");
                html.append("<thead><tr><th>Consignment No</th><th>Sender</th><th>Receiver</th><th>Route</th><th>Type</th><th>Charge</th><th>Status</th><th>Action</th></tr></thead>");
                html.append("<tbody>");
                for (Parcel p : parcels) {
                    html.append("<tr>");
                    html.append("<td><b>").append(p.getConsignmentNo()).append("</b></td>");
                    html.append("<td>").append(p.getSenderName()).append("</td>");
                    html.append("<td>").append(p.getReceiverName()).append("</td>");
                    html.append("<td>").append(p.getSourceBranchName()).append(" &rarr; ").append(p.getDestinationBranchName()).append("</td>");
                    html.append("<td>").append(p.getParcelType().getDisplayName()).append("</td>");
                    html.append("<td>").append(p.getFormattedCharge()).append("</td>");
                    html.append("<td><span class='status status-").append(p.getStatus().name()).append("'>").append(p.getStatus().getDisplayName()).append("</span></td>");
                    html.append("<td><a href='/?cno=").append(p.getConsignmentNo()).append("' style='color:var(--primary); font-weight:600; text-decoration:none;'>Track &rarr;</a></td>");
                    html.append("</tr>");
                }
                html.append("</tbody></table>");
                html.append("</div>");

                html.append("</div></body></html>");

                byte[] bytes = html.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error serving web dashboard: " + e.getMessage(), e);
                String err = "500 Internal Server Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, err.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(err.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query == null) return map;
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), 
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
            return map;
        }
    }

    private static class StatsApiHandler implements HttpHandler {
        private final ParcelService parcelService;

        StatsApiHandler(ParcelService parcelService) {
            this.parcelService = parcelService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                DashboardStats s = parcelService.getDashboardStatistics();
                String json = String.format("{\"total\":%d,\"booked\":%d,\"inTransit\":%d,\"outForDelivery\":%d,\"delivered\":%d,\"pending\":%d,\"revenue\":%.2f}",
                        s.getTotalParcels(), s.getBookedParcels(), s.getInTransitParcels(),
                        s.getOutForDeliveryParcels(), s.getDeliveredParcels(), s.getPendingDeliveries(), s.getTotalRevenue());
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception ex) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        }
    }

    private static class ParcelsApiHandler implements HttpHandler {
        private final ParcelService parcelService;

        ParcelsApiHandler(ParcelService parcelService) {
            this.parcelService = parcelService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Parcel> list = parcelService.getAllParcels();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Parcel p = list.get(i);
                    json.append(String.format("{\"cno\":\"%s\",\"sender\":\"%s\",\"receiver\":\"%s\",\"weight\":%.2f,\"charge\":%.2f,\"status\":\"%s\"}",
                            p.getConsignmentNo(), p.getSenderName(), p.getReceiverName(), p.getWeight(), p.getDeliveryCharge(), p.getStatus().name()));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");
                byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception ex) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        }
    }

    private static class TrackApiHandler implements HttpHandler {
        private final ParcelService parcelService;
        private final TrackingService trackingService;

        TrackApiHandler(ParcelService parcelService, TrackingService trackingService) {
            this.parcelService = parcelService;
            this.trackingService = trackingService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String cno = query != null && query.contains("cno=") ? query.substring(query.indexOf("cno=") + 4).split("&")[0] : "";
                Optional<Parcel> p = parcelService.getParcelByConsignmentNo(cno);
                if (p.isPresent()) {
                    List<TrackingHistory> history = trackingService.getTrackingTimeline(cno);
                    StringBuilder json = new StringBuilder("{");
                    json.append(String.format("\"cno\":\"%s\",\"status\":\"%s\",\"sender\":\"%s\",\"receiver\":\"%s\",\"history\":[",
                            p.get().getConsignmentNo(), p.get().getStatus().name(), p.get().getSenderName(), p.get().getReceiverName()));
                    for (int i = 0; i < history.size(); i++) {
                        TrackingHistory th = history.get(i);
                        json.append(String.format("{\"status\":\"%s\",\"location\":\"%s\",\"remarks\":\"%s\",\"time\":\"%s\"}",
                                th.getStatus().name(), th.getLocation(), th.getRemarks(), th.getFormattedDate()));
                        if (i < history.size() - 1) json.append(",");
                    }
                    json.append("]}");
                    byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    String notFound = "{\"error\":\"Consignment not found\"}";
                    byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(404, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
            } catch (Exception ex) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        }
    }
}
