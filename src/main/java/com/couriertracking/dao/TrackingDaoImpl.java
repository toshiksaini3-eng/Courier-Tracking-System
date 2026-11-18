package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of TrackingDao using PreparedStatement and ResultSet.
 */
public class TrackingDaoImpl implements TrackingDao {

    @Override
    public boolean addTrackingEvent(TrackingHistory event) throws DatabaseException {
        String sql = "INSERT INTO tracking_history (parcel_id, consignment_no, status, location, remarks, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, event.getParcelId());
                ps.setString(2, event.getConsignmentNo());
                ps.setString(3, event.getStatus().name());
                ps.setString(4, event.getLocation() != null ? event.getLocation() : "Transit Hub");
                ps.setString(5, event.getRemarks() != null ? event.getRemarks() : "");
                ps.setTimestamp(6, event.getUpdatedAt());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            event.setId(gk.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record tracking event: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<TrackingHistory> getHistoryByParcelId(int parcelId) throws DatabaseException {
        String sql = "SELECT id, parcel_id, consignment_no, status, location, remarks, updated_at " +
                     "FROM tracking_history WHERE parcel_id = ? ORDER BY updated_at ASC, id ASC";
        List<TrackingHistory> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, parcelId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch tracking history: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrackingHistory> getHistoryByConsignmentNo(String consignmentNo) throws DatabaseException {
        String sql = "SELECT id, parcel_id, consignment_no, status, location, remarks, updated_at " +
                     "FROM tracking_history WHERE UPPER(consignment_no) = UPPER(?) ORDER BY updated_at ASC, id ASC";
        List<TrackingHistory> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, consignmentNo.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch tracking history: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrackingHistory> getRecentActivities(int limit) throws DatabaseException {
        String sql = "SELECT id, parcel_id, consignment_no, status, location, remarks, updated_at " +
                     "FROM tracking_history ORDER BY updated_at DESC, id DESC LIMIT ?";
        List<TrackingHistory> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch recent activities: " + e.getMessage(), e);
        }
        return list;
    }

    private TrackingHistory mapRow(ResultSet rs) throws SQLException {
        TrackingHistory th = new TrackingHistory();
        th.setId(rs.getInt("id"));
        th.setParcelId(rs.getInt("parcel_id"));
        th.setConsignmentNo(rs.getString("consignment_no"));
        th.setStatus(ParcelStatus.fromString(rs.getString("status")));
        th.setLocation(rs.getString("location"));
        th.setRemarks(rs.getString("remarks"));
        th.setUpdatedAt(rs.getTimestamp("updated_at"));
        return th;
    }
}
