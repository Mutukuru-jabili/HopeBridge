package in.hopebridge.web;
import in.hopebridge.model.User;
import in.hopebridge.repository.RewardTransactionRepository;
import in.hopebridge.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController @RequestMapping("/api/wallet") @Transactional
public class WalletController {
 private final CurrentUser current; private final WalletService wallet; private final RewardTransactionRepository transactions;
 public WalletController(CurrentUser c,WalletService w,RewardTransactionRepository t){current=c;wallet=w;transactions=t;}
 @GetMapping Map<String,Object> get(Authentication a){User u=current.get(a);return Map.of("balance",wallet.wallet(u).getBalance(),"history",transactions.findByUserOrderByCreatedAtDesc(u).stream().map(t->Map.of("id",t.getId(),"points",t.getPoints(),"reason",t.getReason(),"createdAt",t.getCreatedAt())).toList());}
}
