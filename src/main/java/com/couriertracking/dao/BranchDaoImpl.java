package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.Branch;
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
 * JDBC implementation of BranchDao using PreparedStatement and ResultSet.
 */
public class BranchDaoImpl implements BranchDao {

    @Override
    public List<Branch> getAllBranches() throws DatabaseException {
        String sql = "SELECT id, branch_code, branch_name, city, state, contact_phone, address, is_active, created_at " +
                     "FROM branches ORDER BY branch_code ASC";
        List<Branch> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch branches: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Branch> getActiveBranches() throws DatabaseException {
        String sql = "SELECT id, branch_code, branch_name, city, state, contact_phone, address, is_active, created_at " +
                     "FROM branches WHERE is_active = 1 ORDER BY branch_name ASC";
        List<Branch> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch active branches: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Branch> getBranchById(int id) throws DatabaseException {
        String sql = "SELECT id, branch_code, branch_name, city, state, contact_phone, address, is_active, created_at " +
                     "FROM branches WHERE id = ?";
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
            throw new DatabaseException("Failed to fetch branch by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Branch> getBranchByCode(String code) throws DatabaseException {
        String sql = "SELECT id, branch_code, branch_name, city, state, contact_phone, address, is_active, created_at " +
                     "FROM branches WHERE branch_code = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch branch by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean insertBranch(Branch branch) throws DatabaseException {
        String sql = "INSERT INTO branches (branch_code, branch_name, city, state, contact_phone, address, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, branch.getBranchCode().trim().toUpperCase());
                ps.setString(2, branch.getBranchName().trim());
                ps.setString(3, branch.getCity().trim());
                ps.setString(4, branch.getState().trim());
                ps.setString(5, branch.getContactPhone().trim());
                ps.setString(6, branch.getAddress().trim());
                ps.setInt(7, branch.isActive() ? 1 : 0);

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            branch.setId(gk.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert branch: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateBranch(Branch branch) throws DatabaseException {
        String sql = "UPDATE branches SET branch_name = ?, city = ?, state = ?, contact_phone = ?, " +
                     "address = ?, is_active = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, branch.getBranchName().trim());
                ps.setString(2, branch.getCity().trim());
                ps.setString(3, branch.getState().trim());
                ps.setString(4, branch.getContactPhone().trim());
                ps.setString(5, branch.getAddress().trim());
                ps.setInt(6, branch.isActive() ? 1 : 0);
                ps.setInt(7, branch.getId());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update branch: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteBranch(int id) throws DatabaseException {
        String sql = "DELETE FROM branches WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete branch (it may be linked to parcels): " + e.getMessage(), e);
        }
    }

    private Branch mapRow(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setId(rs.getInt("id"));
        b.setBranchCode(rs.getString("branch_code"));
        b.setBranchName(rs.getString("branch_name"));
        b.setCity(rs.getString("city"));
        b.setState(rs.getString("state"));
        b.setContactPhone(rs.getString("contact_phone"));
        b.setAddress(rs.getString("address"));
        b.setActive(rs.getInt("is_active") == 1);
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
