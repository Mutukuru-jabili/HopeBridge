package in.hopebridge.service;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WalletService {
    private final WalletRepository wallets; private final RewardTransactionRepository transactions;
    public WalletService(WalletRepository wallets, RewardTransactionRepository transactions) { this.wallets = wallets; this.transactions = transactions; }
    public Wallet wallet(User user) { return wallets.findByUser(user).orElseGet(() -> { Wallet w = new Wallet(); w.setUser(user); return wallets.save(w); }); }
    public void award(User user, int points, String reason) {
        award(user, points, reason, null, null, "SYSTEM");
    }
    public void award(User user, int points, String reason, User admin, CaseFile caseFile, String type) {
        award(user, points, reason, admin, caseFile, null, type);
    }
    public void award(User user, int points, String reason, User admin, CaseFile caseFile, Evidence evidence, String type) {
        if (points <= 0) throw new IllegalArgumentException("Points must be greater than zero.");
        Wallet w = wallet(user); w.setBalance(w.getBalance() + points); wallets.save(w);
        RewardTransaction t = new RewardTransaction(); t.setUser(user); t.setPoints(points); t.setReason(reason);
        t.setAdmin(admin); t.setCaseFile(caseFile); t.setEvidence(evidence); t.setType(type); transactions.save(t);
    }
    public void adjust(User user, int points, String reason, User admin) {
        Wallet w = wallet(user);
        if (w.getBalance() + points < 0) throw new IllegalArgumentException("Adjustment cannot make balance negative.");
        w.setBalance(w.getBalance() + points); wallets.save(w);
        RewardTransaction t = new RewardTransaction(); t.setUser(user); t.setPoints(points);
        t.setReason(reason); t.setAdmin(admin); t.setType("ADMIN_ADJUSTMENT"); transactions.save(t);
    }
}
