package com.rbdip.bookstore.purchase;

/**
 * Контракт модуля покупок для остальных модулей (Strangler Fig, ЛР4).
 */
public interface PurchaseVerifier {

    boolean anyPurchasesExist();
}
