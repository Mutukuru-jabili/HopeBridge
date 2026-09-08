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
        Wallet w = wallet(user); w.setBalance(w.getBalance() + points); wallets.save(w);
        RewardTransaction t = new RewardTransaction(); t.setUser(user); t.setPoints(points); t.setReason(reason); transactions.save(t);
    }
}
