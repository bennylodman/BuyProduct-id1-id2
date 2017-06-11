package il.ac.technion.cs.sd.buy.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import db_utils.DataBaseFactory;
import il.ac.technion.cs.sd.buy.app.BuyProductReader;
import il.ac.technion.cs.sd.buy.app.BuyProductReaderImpl;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by Nadav on 26-May-17.
 */
public class BuyProductReaderImplTest {


    private BuyProductReader SetupAndBuildBookScoreReader(String file_name) throws IOException, SAXException, ParserConfigurationException, InterruptedException, ExecutionException, JSONException {

        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(file_name).getFile())).useDelimiter("\\Z").next();

        Injector injector = Guice.createInjector(new FakeBuyProductModule(), new MockedFutureLineStorageModule());

        FakeBuyProductInitializerImpl fakeBuyProductInitializer = injector.getInstance(FakeBuyProductInitializerImpl.class);
        if (file_name.endsWith("xml"))
            fakeBuyProductInitializer.setupXml(fileContents);
        else {
            assert file_name.endsWith("json");
            fakeBuyProductInitializer.setupJson(fileContents);
        }

        DataBaseFactory dbf = fakeBuyProductInitializer.get_DataBaseFactory();

        return new BuyProductReaderImpl(dbf);
    }
    @Test
    public void isValidOrderId_xml() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<Boolean> val1 = buyProductReader.isValidOrderId("1");
        CompletableFuture<Boolean> val2 = buyProductReader.isValidOrderId("2");
        CompletableFuture<Boolean> val3 = buyProductReader.isValidOrderId("3");
        CompletableFuture<Boolean> val4 = buyProductReader.isValidOrderId("4");

        assertTrue(val1.get());
        assertTrue(val2.get());
        assertTrue(val3.get());
        assertFalse(val4.get());
    }

    @Test
    public void isCanceledOrder_xml() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<Boolean> val1 = buyProductReader.isCanceledOrder("1");
        CompletableFuture<Boolean> val2 = buyProductReader.isCanceledOrder("2");
        CompletableFuture<Boolean> val3 = buyProductReader.isCanceledOrder("3");


        assertTrue(val1.get());
        assertFalse(val2.get());
        assertFalse(val3.get());
    }

    @Test
    public void isModifiedOrder_xml() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<Boolean> val1 = buyProductReader.isModifiedOrder("1");
        CompletableFuture<Boolean> val2 = buyProductReader.isModifiedOrder("2");
        CompletableFuture<Boolean> val3 = buyProductReader.isModifiedOrder("3");


        assertTrue(val1.get());
        assertFalse(val2.get());
        assertFalse(val3.get());
    }

    @Test
    public void getNumberOfProductOrdered_xml() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getNumberOfProductOrdered("1");
        CompletableFuture<?> val2 = buyProductReader.getNumberOfProductOrdered("2");
        CompletableFuture<?> val3 = buyProductReader.getNumberOfProductOrdered("3");
        CompletableFuture<?> val4 = buyProductReader.getNumberOfProductOrdered("4");

        assertEquals(OptionalInt.of(-10),val1.get());
        assertEquals(OptionalInt.of(5),val2.get());
        assertEquals(OptionalInt.of(12),val3.get());
        assertEquals(OptionalInt.empty(),val4.get());

    }

    @Test
    public void getHistoryOfOrder_xml() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");
        CompletableFuture<List<Integer>> list = buyProductReader.getHistoryOfOrder("1");
        assertEquals(3, list.get().size());
        assertEquals(5, (long)(list.get()).get(0));
        assertEquals(10, (long)(list.get()).get(1));
        assertEquals(-1, (long)(list.get()).get(2));

        list = buyProductReader.getHistoryOfOrder("2");
        assertEquals(1, list.get().size());
        assertEquals(5, (long)(list.get()).get(0));



    }

    @Test
    public void getOrderIdsForUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getOrderIdsForUser("1");
        CompletableFuture<?> val2 = buyProductReader.getOrderIdsForUser("2");

        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("2");
        List<String> list2 = new ArrayList<>();
        assertEquals(list1,val1.get());
        assertEquals(list2,val2.get());

    }

    @Test
    public void getTotalAmountSpentByUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getTotalAmountSpentByUser("1");
        CompletableFuture<?> val2 = buyProductReader.getTotalAmountSpentByUser("2");
        CompletableFuture<?> val3 = buyProductReader.getTotalAmountSpentByUser("3");

        assertEquals(500L*10L,val1.get());
        assertEquals(0L,val2.get());
        assertEquals(500L*12L,val3.get());

    }

    @Test
    public void getUsersThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getUsersThatPurchased("android");
        CompletableFuture<?> val2 = buyProductReader.getUsersThatPurchased("iphone");
        CompletableFuture<?> val3 = buyProductReader.getUsersThatPurchased("benny");

        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("3");
        List<String> list2 = new ArrayList<>();

        assertEquals(list1,val1.get());
        assertEquals(list2,val2.get());
        assertEquals(list2,val3.get());
    }

    @Test
    public void getOrderIdsThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getOrderIdsThatPurchased("android");
        CompletableFuture<?> val2 = buyProductReader.getOrderIdsThatPurchased("iphone");
        CompletableFuture<?> val3 = buyProductReader.getOrderIdsThatPurchased("benny");

        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("2");
        list1.add("3");
        List<String> list2 = new ArrayList<>();



        assertEquals(list1,val1.get());
        assertEquals(list2,val2.get());
        assertEquals(list2,val3.get());
    }

    @Test
    public void getTotalNumberOfItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getTotalNumberOfItemsPurchased("android");
        CompletableFuture<?> val2 = buyProductReader.getTotalNumberOfItemsPurchased("iphone");
        CompletableFuture<?> val3 = buyProductReader.getTotalNumberOfItemsPurchased("benny");

        assertEquals(OptionalLong.of(17),val1.get());
        assertEquals(OptionalLong.empty(),val2.get());
        assertEquals(OptionalLong.empty(),val3.get());

    }

    @Test
    public void getAverageNumberOfItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getAverageNumberOfItemsPurchased("android");
        CompletableFuture<?> val2 = buyProductReader.getAverageNumberOfItemsPurchased("iphone");
        CompletableFuture<?> val3 = buyProductReader.getAverageNumberOfItemsPurchased("benny");

        assertEquals(OptionalDouble.of(8.5),val1.get());
        assertEquals(OptionalDouble.empty(),val2.get());
        assertEquals(OptionalDouble.empty(),val3.get());

    }


    @Test
    public void getCancelRatioForUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getCancelRatioForUser("1");
        CompletableFuture<?> val2 = buyProductReader.getCancelRatioForUser("2");

        assertEquals(OptionalDouble.of(0.5),val1.get());
        assertEquals(OptionalDouble.empty(),val2.get());

    }

    @Test
    public void getModifyRatioForUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");

        CompletableFuture<?> val1 = buyProductReader.getModifyRatioForUser("1");
        CompletableFuture<?> val2 = buyProductReader.getModifyRatioForUser("2");

        assertEquals(OptionalDouble.of(0.5),val1.get());
        assertEquals(OptionalDouble.empty(),val2.get());
    }


    @Test
    public void getAllItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");
        Map<String, Long> allItems1 = buyProductReader.getAllItemsPurchased("1").get();
        Map<String, Long> allItems2 = buyProductReader.getAllItemsPurchased("2").get();
        Map<String, Long> allItems3 = buyProductReader.getAllItemsPurchased("3").get();

        assertEquals(1,allItems1.size());
        assertEquals(0,allItems2.size());
        assertEquals(1,allItems3.size());

        assertTrue(allItems1.containsKey("android"));
        assertFalse(allItems2.containsKey("android"));
        assertTrue(allItems3.containsKey("android"));

        assertEquals((long) 5, (long)allItems1.get("android"));
        assertEquals((long) 12, (long)allItems3.get("android"));



    }

    @Test
    public void getItemsPurchasedByUsers() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("small.xml");
        Map<String, Long> allUsers1 = buyProductReader.getItemsPurchasedByUsers("android").get();
        Map<String, Long> allUsers2 = buyProductReader.getItemsPurchasedByUsers("iphone").get();

        assertEquals(2,allUsers1.size());
        assertEquals(0,allUsers2.size());
        assertEquals((long)5, (long)allUsers1.get("1"));
        assertEquals((long)12, (long)allUsers1.get("3"));


    }

    @Test
    public void bigFileTestJson() throws Exception {
       BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("bigOurs.json");

        CompletableFuture<Boolean> val1 = buyProductReader.isModifiedOrder("gg0t");
        CompletableFuture<Boolean> val2 = buyProductReader.isCanceledOrder("0b0n");
        CompletableFuture<List<Integer>> list = buyProductReader.getHistoryOfOrder("3c13");
        CompletableFuture<Map<String,Long>> val3 = buyProductReader.getAllItemsPurchased("0b0n");
        assertFalse(val1.get());
        assertTrue(val2.get());
        assertEquals( 0, (long) list.get().size()); //Does not has product -> order does not spouse to exist;
        assertEquals( 0, (long) val3.get().size());//Does not has product -> order does not spouse to exist;



    }

    @Test
    public void bigFileTestXml() throws Exception {
       BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("bigOurs.xml");

        CompletableFuture<Boolean> val1 = buyProductReader.isModifiedOrder("m3a");
        CompletableFuture<Boolean> val2 = buyProductReader.isCanceledOrder("m3a");
        CompletableFuture<List<Integer>> list = buyProductReader.getHistoryOfOrder("m3a");
        assertTrue(val1.get());
        assertFalse(val2.get());
        assertEquals( 3, (long) list.get().size());
        assertEquals( 83, (long) list.get().get(0));
        assertEquals( 952, (long) list.get().get(1));
        assertEquals( 591, (long) list.get().get(2));
    }

///////////////////////////////////////////////////////////////

    @Test
    public void orderIdNotFound() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isValidOrderId("4").get());
    }

    @Test
    public void orderIdOverriddenByNonExistingProductShouldNotBeFound() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isValidOrderId("6").get());
    }

    @Test
    public void orderIdFoundEvenThoughOverridden() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isValidOrderId("2").get());
    }

    @Test
    public void orderIdFoundEvenThoughModdified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isValidOrderId("1").get());
    }

    @Test
    public void orderIdFoundEvenThoughCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isValidOrderId("3").get());
    }

    @Test
    public void orderIdFound() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isValidOrderId("5").get());
    }

    @Test
    public void cancelledOrderFoundAsCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isCanceledOrder("3").get());
    }

    @Test
    public void nonCancelledOrderShouldNotBeCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isCanceledOrder("5").get());
        Assert.assertFalse(buyProductReader.isCanceledOrder("1").get());
    }

    @Test
    public void nonExistingOrderShouldNotBeCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isCanceledOrder("4").get());
    }

    @Test
    public void cancelledOrderOverriddenByOtherOrderShouldNotBeCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isCanceledOrder("7").get());
        Assert.assertFalse(buyProductReader.isCanceledOrder("8").get());
    }

    @Test
    public void cancelledOrderSeveralTimesFoundAsCancelled() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isCanceledOrder("9").get());
    }

    @Test
    public void modifiedOrderFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isModifiedOrder("1").get());
    }

    @Test
    public void nonModifiedOrderShouldNotBeFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isModifiedOrder("3").get());
        Assert.assertFalse(buyProductReader.isModifiedOrder("5").get());
    }

    @Test
    public void modifiedSeveralTimesOrderShouldeFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isModifiedOrder("7").get());
    }

    @Test
    public void nonExistingOrderShouldNotBeFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isModifiedOrder("4").get());
    }

    @Test
    public void invalidOrderShouldeNotFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isModifiedOrder("6").get());
    }

    @Test
    public void modifiedButOverriddenOrderShouldeNotFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertFalse(buyProductReader.isModifiedOrder("8").get());
    }

    @Test
    public void modifiedAndCancelledOrderShouldeFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isModifiedOrder("9").get());
    }

    @Test
    public void cancelledOrderOverriddenByModifiedOrderShouldBeFoundAsModified() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertTrue(buyProductReader.isModifiedOrder("7").get());
    }

    @Test
    public void validOrderShouldReturnValidAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.of(5), buyProductReader.getNumberOfProductOrdered("5").get());
    }

    @Test
    public void severalModifiedOrdersShouldReturnLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.of(500000), buyProductReader.getNumberOfProductOrdered("1").get());
        Assert.assertEquals(OptionalInt.of(50), buyProductReader.getNumberOfProductOrdered("7").get());
    }

    @Test
    public void cancelledOrdersShouldReturnNegationOfAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.of(-10), buyProductReader.getNumberOfProductOrdered("3").get());
    }

    @Test
    public void sevrelCancelledOrdersOfSeveralModifiedOrdersShouldReturnNegationOfLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.of(-51), buyProductReader.getNumberOfProductOrdered("9").get());
    }

    @Test
    public void overriddenOrdersShouldReturnLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.of(10), buyProductReader.getNumberOfProductOrdered("2").get());
        Assert.assertEquals(OptionalInt.of(8), buyProductReader.getNumberOfProductOrdered("8").get());
    }

    @Test
    public void nonExistingOrderShouldNotReturnAnyAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.empty(), buyProductReader.getNumberOfProductOrdered("4").get());
    }

    @Test
    public void invalidOrderShouldNotReturnAnyAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalInt.empty(), buyProductReader.getNumberOfProductOrdered("6").get());
    }

    @Test
    public void singleOrderHistoryShouldBeWithCorrectValue() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("5").get();
        Assert.assertEquals(1, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(5));
    }

    @Test
    public void modifiedOrderShouldAppendNewAmountToHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("1").get();
        Assert.assertEquals(2, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(5, 500000));
    }

    @Test
    public void cancelledOrderShouldAppendMinusOneToHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("3").get();
        Assert.assertEquals(2, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(10, -1));
    }

    @Test
    public void overridingOrderShouldOverrideHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("2").get();
        Assert.assertEquals(1, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(10));
        res = buyProductReader.getHistoryOfOrder("8").get();
        Assert.assertEquals(1, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(8));
    }

    @Test
    public void severalModifiedOrdersShouldAllBeAppendedToHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("7").get();
        Assert.assertEquals(3, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(5, 2, 50));
    }

    @Test
    public void severalCancelationsOrdersShouldOnlyBeAppendedOnceToHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("9").get();
        Assert.assertEquals(3, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(5, 51, -1));
    }

    @Test
    public void modifiedAfterCancelShouldNotOverriddeHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<Integer> res = buyProductReader.getHistoryOfOrder("10").get();
        Assert.assertEquals(7, res.size());
        assertThat(res, IsIterableContainingInOrder.contains(1, 100, 6, 18, 7, 2, -1));
    }

    @Test
    public void nonExistingOrderShouldNotContainHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getHistoryOfOrder("4").get(), is(empty()));
    }

    @Test
    public void invalidOrderShouldNotContainHistory() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getHistoryOfOrder("6").get(), is(empty()));
    }

    @Test
    public void userMadeOneOrder() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<String> result = buyProductReader.getOrderIdsForUser("7").get();
        Assert.assertEquals(1, result.size());
        assertThat(result, IsIterableContainingInOrder.contains("5"));
    }

    @Test
    public void severalOrdersForUserShouldBeSorted() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<String> result = buyProductReader.getOrderIdsForUser("2").get();
        Assert.assertEquals(2, result.size());
        assertThat(result, IsIterableContainingInOrder.contains("12", "7"));
    }

    @Test
    public void cancelledOrdersMadeByUserShouldStillCount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        List<String> result = buyProductReader.getOrderIdsForUser("5").get();
        Assert.assertEquals(3, result.size());
        assertThat(result, IsIterableContainingInOrder.contains("10", "3", "9"));
    }

    @Test
    public void overriddenOrdersMadeByDifferentUserShouldOnlyAppearOnLastUserList() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(1, buyProductReader.getOrderIdsForUser("1").get().size());
        assertThat(buyProductReader.getOrderIdsForUser("1").get(), IsIterableContainingInOrder.contains("2"));
        Assert.assertEquals(2, buyProductReader.getOrderIdsForUser("3").get().size());
        assertThat(buyProductReader.getOrderIdsForUser("3").get(), IsIterableContainingInOrder.contains("1", "8"));
        assertThat(buyProductReader.getOrderIdsForUser("9").get(), is(empty()));
    }

    @Test
    public void nonExistingUserShouldHaveNoOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getOrderIdsForUser("4").get(), is(empty()));
    }
    @Test
    public void nonExistingProductOrderedByUserShouldNotAppear() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getOrderIdsForUser("6").get(), is(empty()));
    }

    @Test
    public void userMadeOneOrderShouldGetTheProductPriceTimesAmountOrdered() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(500),buyProductReader.getTotalAmountSpentByUser("7").get());
    }
    @Test
    public void userMadeSeveralOrdersWithModifiesShouldGetSumOfProductPriceTimesLastAmountOrdered() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(10005000),buyProductReader.getTotalAmountSpentByUser("2").get());
    }
    @Test
    public void userCancelledAllHisOrdersShouldGetZero() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(0),buyProductReader.getTotalAmountSpentByUser("5").get());
    }
    @Test
    public void overriddenOrdersMadeByDifferentUserShouldGetSumOfProductPriceTimesLastAmountOrdered() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(100),buyProductReader.getTotalAmountSpentByUser("1").get());
    }
    @Test
    public void userOrderedTooManyIphonesShouldDeclareBankruptcy () throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(500008000000L),buyProductReader.getTotalAmountSpentByUser("3").get());
    }
    @Test
    public void nonExistingUserShouldNotPay() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(0),buyProductReader.getTotalAmountSpentByUser("4").get());
    }
    @Test
    public void userOrderedNonExistingProductShouldNotPay() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(Long.valueOf(0),buyProductReader.getTotalAmountSpentByUser("6").get());
    }
    @Test
    public void userWithNoCancelledOrdersShouldHaveRatioOfZero() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(0.0),buyProductReader.getCancelRatioForUser("3").get());
        Assert.assertEquals(OptionalDouble.of(0.0),buyProductReader.getCancelRatioForUser("1").get());
    }
    @Test
    public void userWhoCancelledSomeOfHisOrdersShouldHaveCorrectRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(1/(double)3),buyProductReader.getCancelRatioForUser("8").get());
    }
    @Test
    public void userWhoCancelledAllOfHisOrdersShouldHaveRatioOfOne() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(1),buyProductReader.getCancelRatioForUser("5").get());
    }
    @Test
    public void userWhosOrderWasStolenShouldHaveNoCancelRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getCancelRatioForUser("9").get());
    }
    @Test
    public void nonExistingUserShouldHaveNoCancelRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getCancelRatioForUser("4").get());
    }
    @Test
    public void userOrderedNonExistingProductShouldHaveNoCancelRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getCancelRatioForUser("6").get());
    }

    @Test
    public void userWithNoModifiedOrdersShouldHaveRatioOfZero() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(0.0),buyProductReader.getModifyRatioForUser("7").get());
        Assert.assertEquals(OptionalDouble.of(0.0),buyProductReader.getModifyRatioForUser("1").get());
    }
    @Test
    public void userWhoModifiedSomeOfHisOrdersShouldHaveCorrectRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(0.5),buyProductReader.getModifyRatioForUser("3").get());
    }
    @Test
    public void userWhoModifiedAllOfHisOrdersShouldHaveRatioOfOne() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(1),buyProductReader.getModifyRatioForUser("2").get());
    }
    @Test
    public void userWhosOrderWasStolenShouldHaveNoModifiedRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getModifyRatioForUser("9").get());
    }

    @Test
    public void cancelledOrderShouldNotChangeModifiedRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.of(2/(double)3),buyProductReader.getModifyRatioForUser("5").get());
    }

    @Test
    public void nonExistingUserShouldHaveNoModifiedRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getModifyRatioForUser("4").get());
    }
    @Test
    public void userOrderedNonExistingProductShouldHaveNoModifiedRatio() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getModifyRatioForUser("6").get());
    }

    @Test
    public void userMadeOneOrderShouldGetCorrectProductAndAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("android",5L);
        assertThat(buyProductReader.getAllItemsPurchased("7").get(),equalTo(expected));
        expected.clear();
        expected.put("nokia",10L);
        assertThat(buyProductReader.getAllItemsPurchased("1").get(),equalTo(expected));
    }
    @Test
    public void userMadeSeveralOrdersShouldGetAllProductAndAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("android",50L);
        expected.put("iphone",10L);
        assertThat(buyProductReader.getAllItemsPurchased("2").get(),equalTo(expected));
    }
    @Test
    public void userCanceledAllOrdersShouldHaveNoProducts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getAllItemsPurchased("5").get().entrySet(),empty());
    }

    @Test
    public void userOrderedSameProductSeveralTimes() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("iphone",500008L);
        assertThat(buyProductReader.getAllItemsPurchased("3").get(),equalTo(expected));
    }

    @Test
    public void userModifiedOrderShouldReturnLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("nokia",25L);
        assertThat(buyProductReader.getAllItemsPurchased("8").get(),equalTo(expected));
    }
    @Test
    public void userWhosOrderWasStolenShouldHaveNoProducts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getAllItemsPurchased("9").get().entrySet(),empty());
    }
    @Test
    public void nonExistingUserShouldHaveNoProducts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getAllItemsPurchased("4").get().entrySet(),empty());
    }
    @Test
    public void userOrderedNonExistingProductShouldHaveNoProducts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.xml");
        assertThat(buyProductReader.getAllItemsPurchased("6").get().entrySet(),empty());
    }

    @Test
    public void productOrderedOnlyOnceByOneUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("android").get(), IsIterableContainingInOrder.contains("3"));
    }
    @Test
    public void productOrderedBySeveralUsersShouldReturnSortedUsers() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("iphone").get(), IsIterableContainingInOrder.contains("1","2"));
    }
    @Test
    public void productOrderedByUserAndThenCancelledShouldReturnNoUsers() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("nokia").get(),is(empty()));
    }
    @Test
    public void productOrderedByUserAndThenTheOrderWasOverriddenWithAnotherProductShouldNotAppearForUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("idroid").get(),is(empty()));
    }
    @Test
    public void productOrderedByUserAndThenModifiedShouldReturnUsers() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("dell").get(), IsIterableContainingInOrder.contains("1"));
    }
    @Test
    public void productNeverOrderedByUserShouldHaveNoUsersThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("mac").get(),is(empty()));
    }
    @Test
    public void nonExistingProductShouldHaveNoUsersThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getUsersThatPurchased("not a mac").get(),is(empty()));
    }
    @Test
    public void productOrderedOnlyOnceInOneOrder() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("android").get(), IsIterableContainingInOrder.contains("1"));
    }
    @Test
    public void productOrderedInSeveralOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("iphone").get(), IsIterableContainingInOrder.contains("2","3"));
    }

    @Test
    public void productOrderedAndThenModifiedShouldReturnOrder() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("dell").get(), IsIterableContainingInOrder.contains("7"));
    }

    @Test
    public void productOrderedByUserAndThenCancelledShouldReturnTheOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("nokia").get(), IsIterableContainingInOrder.contains("5"));
    }
    @Test
    public void productOrderedOnceAndThenOverriddenWithAnotherProductShouldNotAppearForOrder() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("idroid").get(), is(empty()));
    }
    @Test
    public void productNeverOrderedShouldHaveNoOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("mac").get(),is(empty()));
    }
    @Test
    public void nonExistingProductShouldHaveNoOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getOrderIdsThatPurchased("not a mac").get(),is(empty()));
    }

    @Test
    public void productOrderedOnceShouldReturnCorrectAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.of(10L),buyProductReader.getTotalNumberOfItemsPurchased("android").get());
        Assert.assertEquals(OptionalDouble.of(10),buyProductReader.getAverageNumberOfItemsPurchased("android").get());
    }
    @Test
    public void productOrderedSeveralTimesShouldReturnSumOfAmounts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.of(29L),buyProductReader.getTotalNumberOfItemsPurchased("iphone").get());
        Assert.assertEquals(OptionalDouble.of(14.5),buyProductReader.getAverageNumberOfItemsPurchased("iphone").get());
    }
    @Test
    public void productOrderedAndThenModifiedShouldReturnLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.of(7L),buyProductReader.getTotalNumberOfItemsPurchased("dell").get());
        Assert.assertEquals(OptionalDouble.of(7),buyProductReader.getAverageNumberOfItemsPurchased("dell").get());
    }
    @Test
    public void productOrderedAndThenCancelledShouldHaveNoItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.empty(),buyProductReader.getTotalNumberOfItemsPurchased("nokia").get());
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getAverageNumberOfItemsPurchased("nokia").get());
    }
    @Test
    public void productOrderedOnceAndThenOverriddenWithAnotherProductShouldHaveNoItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.empty(),buyProductReader.getTotalNumberOfItemsPurchased("idroid").get());
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getAverageNumberOfItemsPurchased("idroid").get());

    }
    @Test
    public void productNeverOrderedShouldHaveNoItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.empty(),buyProductReader.getTotalNumberOfItemsPurchased("mac").get());
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getAverageNumberOfItemsPurchased("mac").get());
    }
    @Test
    public void nonExistingProductShouldHaveNoItemsPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.empty(),buyProductReader.getTotalNumberOfItemsPurchased("not a mac").get());
        Assert.assertEquals(OptionalDouble.empty(),buyProductReader.getAverageNumberOfItemsPurchased("not a mac").get());
    }

    @Test
    public void productOrderdAndThenModifiedSeveralTimesShouldReturnLastAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Assert.assertEquals(OptionalLong.of(2L),buyProductReader.getTotalNumberOfItemsPurchased("pizza").get());
        Assert.assertEquals(OptionalDouble.of(2.0),buyProductReader.getAverageNumberOfItemsPurchased("pizza").get());
    }

    @Test
    public void productOrderedByOneUserShouldReturnTheUserAndTheAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("3",10L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("android").get(),equalTo(expected));
    }
    @Test
    public void productOrderedBySeveralUsersShouldReturnAllUsers() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("2",12L);
        expected.put("1",17L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("iphone").get(),equalTo(expected));
    }
    @Test
    public void productOrderedAndThenCancelledShouldNotReturnUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getItemsPurchasedByUsers("nokia").get().entrySet(),empty());
    }
    @Test
    public void productOrderedAndThenModifiedShouldReturnUpdatedAmount() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("1",7L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("dell").get(),equalTo(expected));
        expected.clear();
        expected.put("1",2L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("pizza").get(),equalTo(expected));
    }

    @Test
    public void productOrderedSeveralTimesBySameUserShouldReturnSumOfOrders() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("addict",781L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("drugs").get(),equalTo(expected));
    }

    @Test
    public void productOrderedByDifferentUsersWithMultipleChangesShouldReturnCorrectAmounts() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("addict2",2000L);
        expected.put("addict3",5050L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("more drugs").get(),equalTo(expected));
    }

    @Test
    public void productOrderedWithSameOrderIdByDifferentUsersShouldOnlyAppearForLastUser() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        Map<String,Long> expected = new TreeMap<>();
        expected.put("addict5",50L);
        assertThat(buyProductReader.getItemsPurchasedByUsers("more").get(),equalTo(expected));
    }
    @Test
    public void productNeverOrderedShouldHaveNoUsersThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getItemsPurchasedByUsers("mac").get().entrySet(),empty());
    }
    @Test
    public void nonExistingProductShouldHaveNoUsersMapThatPurchased() throws Exception {
        BuyProductReader buyProductReader = SetupAndBuildBookScoreReader("ourData.json");
        assertThat(buyProductReader.getItemsPurchasedByUsers("not a mac").get().entrySet(),empty());

    }
}