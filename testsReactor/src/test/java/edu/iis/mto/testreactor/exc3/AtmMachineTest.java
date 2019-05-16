package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtmMachineTest {

    AtmMachine atmMachine;

    //data
    Money amount;
    Card card;
    AuthenticationToken authenticationToken;

    Optional<AuthenticationToken> token;
    Optional<AuthenticationToken> emptyToken;
    List<Banknote> banknoteList;

    //mocks
    CardProviderService cardProviderService;
    BankService bankService;
    MoneyDepot moneyDepot;

    @Before
    public void setup(){
        cardProviderService = Mockito.mock(CardProviderService.class);
        bankService = Mockito.mock(BankService.class);
        moneyDepot = Mockito.mock(MoneyDepot.class);

        authenticationToken = AuthenticationToken.builder().withUserId("1").withAuthorizationCode(1234).build();
        token = Optional.of(authenticationToken);
        emptyToken = Optional.empty();

        amount = Money.builder().withAmount(120).withCurrency(Currency.PL).build();
        card = Card.builder().withCardNumber("123").withPinNumber(1234).build();

        banknoteList = new ArrayList<>();

    }

    @Test
    public void cardProviderServiceAuthorizeMethodShouldBeCallceOnce(){

        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(token);
        Mockito.when(bankService.charge(authenticationToken,amount)).thenReturn(true);
        Mockito.when(moneyDepot.releaseBanknotes(Mockito.any())).thenReturn(true);

        Payment paymentFromAtmMachine = atmMachine.withdraw(amount,card);

        Mockito.verify(cardProviderService,Mockito.times(1)).authorize(card);

    }

    @Test (expected = WrongMoneyAmountException.class)
    public void paymentFromAtmMachineShouldCauseWrongMoneyAmountException() {
        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(token);
        Mockito.when(bankService.charge(authenticationToken,amount)).thenReturn(true);
        Mockito.when(moneyDepot.releaseBanknotes(Mockito.any())).thenReturn(true);

        Money negativeAmount = Money.builder().withAmount(-123).withCurrency(Currency.PL).build();

        Payment paymentFromAtmMachine = atmMachine.withdraw(negativeAmount,card);
    }

    @Test (expected = MoneyDepotException.class)
    public void paymentFromAtmMachineShouldCauseMoneyDepotException(){
        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(token);
        Mockito.when(bankService.charge(authenticationToken,amount)).thenReturn(true);
        Mockito.when(moneyDepot.releaseBanknotes(Mockito.any())).thenReturn(false);

        Payment paymentFromAtmMachine = atmMachine.withdraw(amount,card);
    }

    @Test (expected = InsufficientFundsException.class)
    public void paymentFromAtmMachineShouldCauseInsufficientFoundsException(){
        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(token);
        Mockito.when(bankService.charge(authenticationToken,amount)).thenReturn(false);

        Payment paymentFromAtmMachine = atmMachine.withdraw(amount,card);
    }

    @Test (expected = CardAuthorizationException.class)
    public void paymentFromAtmMachineShouldCauseCardAuthorizationException(){
        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(emptyToken);

        Payment paymentFromAtmMachine = atmMachine.withdraw(amount,card);
    }

    @Test
    public void atmMachineShouldReturnAppropriateBankntos(){
        atmMachine = new AtmMachine(cardProviderService,bankService,moneyDepot);

        Mockito.when(cardProviderService.authorize(card)).thenReturn(token);
        Mockito.when(bankService.charge(authenticationToken,amount)).thenReturn(true);
        Mockito.when(moneyDepot.releaseBanknotes(Mockito.any())).thenReturn(true);

        Payment paymentFromAtmMachine = atmMachine.withdraw(amount,card);

        banknoteList.add(Banknote.PL100);
        banknoteList.add(Banknote.PL20);

        Payment expectedPayment = new Payment(banknoteList);

        Assert.assertEquals(expectedPayment,paymentFromAtmMachine);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

}
