package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.Branch;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Branch entities.
 * Demonstrates Abstraction and Interface Segregation.
 */
public interface BranchDao {
    List<Branch> getAllBranches() throws DatabaseException;
    List<Branch> getActiveBranches() throws DatabaseException;
    Optional<Branch> getBranchById(int id) throws DatabaseException;
    Optional<Branch> getBranchByCode(String code) throws DatabaseException;
    boolean insertBranch(Branch branch) throws DatabaseException;
    boolean updateBranch(Branch branch) throws DatabaseException;
    boolean deleteBranch(int id) throws DatabaseException;
}
