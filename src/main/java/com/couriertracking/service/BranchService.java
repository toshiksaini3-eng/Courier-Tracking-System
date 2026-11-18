package com.couriertracking.service;

import com.couriertracking.dao.BranchDao;
import com.couriertracking.dao.BranchDaoImpl;
import com.couriertracking.exception.DatabaseException;
import com.couriertracking.exception.ValidationException;
import com.couriertracking.model.Branch;
import com.couriertracking.util.ValidationUtil;
import java.util.List;
import java.util.Optional;

/**
 * Business service for Branch Management.
 */
public class BranchService {
    private final BranchDao branchDao;

    public BranchService() {
        this.branchDao = new BranchDaoImpl();
    }

    public BranchService(BranchDao branchDao) {
        this.branchDao = branchDao;
    }

    public List<Branch> getAllBranches() throws DatabaseException {
        return branchDao.getAllBranches();
    }

    public List<Branch> getActiveBranches() throws DatabaseException {
        return branchDao.getActiveBranches();
    }

    public Optional<Branch> getBranchById(int id) throws DatabaseException {
        return branchDao.getBranchById(id);
    }

    public void createBranch(Branch branch) throws ValidationException, DatabaseException {
        ValidationUtil.validateNotEmpty(branch.getBranchCode(), "Branch Code");
        ValidationUtil.validateNotEmpty(branch.getBranchName(), "Branch Name");
        ValidationUtil.validateNotEmpty(branch.getCity(), "City");
        ValidationUtil.validateNotEmpty(branch.getState(), "State");
        ValidationUtil.validatePhone(branch.getContactPhone(), "Contact Phone");
        ValidationUtil.validateNotEmpty(branch.getAddress(), "Address");

        // Check for duplicate branch code
        Optional<Branch> existing = branchDao.getBranchByCode(branch.getBranchCode());
        if (existing.isPresent()) {
            throw new ValidationException("Branch Code '" + branch.getBranchCode() + "' is already registered.");
        }

        branchDao.insertBranch(branch);
    }

    public void updateBranch(Branch branch) throws ValidationException, DatabaseException {
        ValidationUtil.validateNotEmpty(branch.getBranchName(), "Branch Name");
        ValidationUtil.validateNotEmpty(branch.getCity(), "City");
        ValidationUtil.validateNotEmpty(branch.getState(), "State");
        ValidationUtil.validatePhone(branch.getContactPhone(), "Contact Phone");
        ValidationUtil.validateNotEmpty(branch.getAddress(), "Address");

        branchDao.updateBranch(branch);
    }

    public void deleteBranch(int id) throws DatabaseException {
        branchDao.deleteBranch(id);
    }
}
