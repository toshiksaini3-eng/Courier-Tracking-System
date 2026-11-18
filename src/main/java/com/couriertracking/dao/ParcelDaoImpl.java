package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.ParcelType;
import com.couriertracking.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of ParcelDao using PreparedStatement and ResultSet.
 */
public class ParcelDaoImpl implements ParcelDao {

    private static final String BASE_SELECT = 
            "SELECT p.id, p.consignment_no, p.sender_name, p.sender_phone, p.sender_address, " +
            "p.receiver_name, p.receiver_phone, p.receiver_address, " +
            "p.source_branch_id, sb.branch_name AS source_branch_name, " +
            "p.destination_branch_id, db.branch_name AS destination_branch_name, " +
            "p.parcel_type, p.weight, p.description, p.delivery_charge, p.status, " +
            "p.booking_date, p.created_at, p.updated_at " +
            "FROM parcels p " +
            "LEFT JOIN branches sb ON p.source_branch_id = sb.id " +
            "LEFT JOIN branches db ON p.destination_branch_id = db.id ";

    @Override
    public boolean insertParcel(Parcel parcel) throws DatabaseException {
        String sql = "INSERT INTO parcels (consignment_no, sender_name, sender_phone, sender_address, " +
                     "receiver_name, receiver_phone, receiver_address, source_branch_id, destination_branch_id, " +
                     "parcel_type, weight, description, delivery_charge, status, booking_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, parcel.getConsignmentNo());
                ps.setString(2, parcel.getSenderName());
                ps.setString(3, parcel.getSenderPhone());
                ps.setString(4, parcel.getSenderAddress());
                ps.setString(5, parcel.getReceiverName());
                ps.setString(6, parcel.getReceiverPhone());
                ps.setString(7, parcel.getReceiverAddress());
                ps.setInt(8, parcel.getSourceBranchId());
                ps.setInt(9, parcel.getDestinationBranchId());
                ps.setString(10, parcel.getParcelType().name());
                ps.setDouble(11, parcel.getWeight());
                ps.setString(12, parcel.getDescription() != null ? parcel.getDescription() : "");
                ps.setDouble(13, parcel.getDeliveryCharge());
                ps.setString(14, parcel.getStatus() != null ? parcel.getStatus().name() : ParcelStatus.BOOKED.name());
                ps.setTimestamp(15, parcel.getBookingDate());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            parcel.setId(gk.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save parcel to database: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<Parcel> getParcelById(int id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving parcel by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Parcel> getParcelByConsignmentNo(String consignmentNo) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE UPPER(p.consignment_no) = UPPER(?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, consignmentNo.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching parcel by consignment number: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Parcel> getAllParcels() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY p.booking_date DESC, p.id DESC";
        List<Parcel> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch parcels: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Parcel> searchParcels(String query, ParcelStatus statusFilter, Integer branchIdFilter) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (p.consignment_no LIKE ? OR p.sender_name LIKE ? OR p.receiver_name LIKE ? OR p.sender_phone LIKE ? OR p.receiver_phone LIKE ?) ");
            String q = "%" + query.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(q);
            }
        }

        if (statusFilter != null) {
            sql.append("AND p.status = ? ");
            params.add(statusFilter.name());
        }

        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append("AND (p.source_branch_id = ? OR p.destination_branch_id = ?) ");
            params.add(branchIdFilter);
            params.add(branchIdFilter);
        }

        sql.append("ORDER BY p.booking_date DESC, p.id DESC");

        List<Parcel> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to filter parcels: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Parcel> getRecentParcels(int limit) throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY p.booking_date DESC, p.id DESC LIMIT ?";
        List<Parcel> list = new ArrayList<>();
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
            throw new DatabaseException("Failed to fetch recent parcels: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean updateParcelStatus(int parcelId, ParcelStatus status) throws DatabaseException {
        String sql = "UPDATE parcels SET status = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                ps.setInt(2, parcelId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update parcel status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteParcel(int parcelId) throws DatabaseException {
        String sql = "DELETE FROM parcels WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, parcelId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete parcel: " + e.getMessage(), e);
        }
    }

    @Override
    public DashboardStats getDashboardStatistics() throws DatabaseException {
        String sql = "SELECT " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'BOOKED' THEN 1 ELSE 0 END) AS booked, " +
                     "SUM(CASE WHEN status = 'IN_TRANSIT' THEN 1 ELSE 0 END) AS in_transit, " +
                     "SUM(CASE WHEN status = 'OUT_FOR_DELIVERY' THEN 1 ELSE 0 END) AS out_for_delivery, " +
                     "SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) AS delivered, " +
                     "SUM(CASE WHEN status NOT IN ('DELIVERED', 'RETURNED') THEN 1 ELSE 0 END) AS pending, " +
                     "SUM(delivery_charge) AS total_revenue " +
                     "FROM parcels";

        DashboardStats stats = new DashboardStats();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalParcels(rs.getInt("total"));
                    stats.setBookedParcels(rs.getInt("booked"));
                    stats.setInTransitParcels(rs.getInt("in_transit"));
                    stats.setOutForDeliveryParcels(rs.getInt("out_for_delivery"));
                    stats.setDeliveredParcels(rs.getInt("delivered"));
                    stats.setPendingDeliveries(rs.getInt("pending"));
                    stats.setTotalRevenue(rs.getDouble("total_revenue"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to compute dashboard statistics: " + e.getMessage(), e);
        }
        return stats;
    }

    private Parcel mapRow(ResultSet rs) throws SQLException {
        Parcel p = new Parcel();
        p.setId(rs.getInt("id"));
        p.setConsignmentNo(rs.getString("consignment_no"));
        p.setSenderName(rs.getString("sender_name"));
        p.setSenderPhone(rs.getString("sender_phone"));
        p.setSenderAddress(rs.getString("sender_address"));
        p.setReceiverName(rs.getString("receiver_name"));
        p.setReceiverPhone(rs.getString("receiver_phone"));
        p.setReceiverAddress(rs.getString("receiver_address"));
        p.setSourceBranchId(rs.getInt("source_branch_id"));
        p.setSourceBranchName(rs.getString("source_branch_name"));
        p.setDestinationBranchId(rs.getInt("destination_branch_id"));
        p.setDestinationBranchName(rs.getString("destination_branch_name"));
        p.setParcelType(ParcelType.fromString(rs.getString("parcel_type")));
        p.setWeight(rs.getDouble("weight"));
        p.setDescription(rs.getString("description"));
        p.setDeliveryCharge(rs.getDouble("delivery_charge"));
        p.setStatus(ParcelStatus.fromString(rs.getString("status")));
        p.setBookingDate(rs.getTimestamp("booking_date"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
