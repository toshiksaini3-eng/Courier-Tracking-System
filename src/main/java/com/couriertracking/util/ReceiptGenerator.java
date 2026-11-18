package com.couriertracking.util;

import com.couriertracking.model.Parcel;

/**
 * Utility to generate formatted printable invoice receipts and airway bills.
 */
public final class ReceiptGenerator {
    private ReceiptGenerator() {}

    public static String generateReceiptText(Parcel parcel) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("                 CRX COURIER EXPRESS                      \n");
        sb.append("           OFFICIAL CONSIGNMENT AIRWAY BILL               \n");
        sb.append("============================================================\n");
        sb.append(String.format(" Consignment No  : %s\n", parcel.getConsignmentNo()));
        sb.append(String.format(" Booking Date    : %s\n", parcel.getFormattedBookingDate()));
        sb.append(String.format(" Current Status  : %s\n", parcel.getStatus().getDisplayName()));
        sb.append("------------------------------------------------------------\n");
        sb.append(" SENDER DETAILS:\n");
        sb.append(String.format("   Name    : %s\n", parcel.getSenderName()));
        sb.append(String.format("   Phone   : %s\n", parcel.getSenderPhone()));
        sb.append(String.format("   Address : %s\n", parcel.getSenderAddress()));
        sb.append(String.format("   Origin  : %s\n", parcel.getSourceBranchName() != null ? parcel.getSourceBranchName() : "Branch #" + parcel.getSourceBranchId()));
        sb.append("------------------------------------------------------------\n");
        sb.append(" RECEIVER DETAILS:\n");
        sb.append(String.format("   Name        : %s\n", parcel.getReceiverName()));
        sb.append(String.format("   Phone       : %s\n", parcel.getReceiverPhone()));
        sb.append(String.format("   Address     : %s\n", parcel.getReceiverAddress()));
        sb.append(String.format("   Destination : %s\n", parcel.getDestinationBranchName() != null ? parcel.getDestinationBranchName() : "Branch #" + parcel.getDestinationBranchId()));
        sb.append("------------------------------------------------------------\n");
        sb.append(" SHIPMENT SPECIFICATIONS:\n");
        sb.append(String.format("   Parcel Type : %s\n", parcel.getParcelType().getDisplayName()));
        sb.append(String.format("   Weight      : %.2f kg\n", parcel.getWeight()));
        sb.append(String.format("   Description : %s\n", parcel.getDescription() != null ? parcel.getDescription() : "Standard Parcel"));
        sb.append("------------------------------------------------------------\n");
        sb.append(" BILLING SUMMARY:\n");
        sb.append(String.format("   Total Charge   : %s\n", parcel.getFormattedCharge()));
        sb.append("   Payment Status : PAID (Prepaid)\n");
        sb.append("============================================================\n");
        sb.append(" Track this shipment live using Consignment No in CRX Portal\n");
        sb.append(" Customer Helpline: 1800-CRX-COURIER | support@crxexpress.com\n");
        sb.append("============================================================\n");
        return sb.toString();
    }
}
