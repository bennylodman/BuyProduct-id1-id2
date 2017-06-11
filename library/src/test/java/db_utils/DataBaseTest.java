package db_utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Nadav on 19-May-17.
 */
public class DataBaseTest {
//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(30);

    public DataBase SetupAndBuildDataBase(Integer num_of_keys,List<String> names_of_columns,LinkedList<String> dataList, Boolean allowMultipuls)
    {

        Injector injector= Guice.createInjector(new MockedFutureLineStorageModule());
        DataBaseFactory dataBaseFactoryMock= injector.getInstance(DataBaseFactoryImpl.class);

        DataBase DB = null;
        DB = dataBaseFactoryMock.setNames_of_columns(names_of_columns)
                .setNum_of_keys(num_of_keys)
                .setDb_name("Testing")
                .setAllow_Multiples(allowMultipuls)
                .build();

        DB.build_db(dataList);

        return DB;
    }

    @Test
    public void build_db_1_key() throws Exception {

        Integer num_of_keys=1;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,9");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data, false);

        Integer val1= DB.getNum_of_keys();

        Optional<Integer> val2= Optional.ofNullable(DB.getNum_of_columns());
        List<String> val3= DB.getNames_of_columns();
        OptionalInt val4= DB.get_num_of_column("Reviewer");
        OptionalInt val5= DB.get_num_of_column("Book");
        OptionalInt val6= DB.get_num_of_column("Score");
        OptionalInt val7= DB.get_num_of_column("Bla");


        assertEquals(num_of_keys,val1);
        assertEquals(OptionalInt.of(names_of_columns.size()),OptionalInt.of(val2.get()));
        assertEquals(names_of_columns,val3);
        assertEquals(OptionalInt.of(0),val4);
        assertEquals(OptionalInt.of(1),val5);
        assertEquals(OptionalInt.of(2),val6);
        assertEquals(OptionalInt.empty(),val7);

    }

    @Test
    public void build_db_2_keys() throws Exception {

        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,9");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data, false);

        assertEquals(num_of_keys,DB.getNum_of_keys());
        assertEquals(OptionalInt.of(names_of_columns.size()),OptionalInt.of(DB.getNum_of_columns()));
        assertEquals(names_of_columns,DB.getNames_of_columns());
        assertEquals(OptionalInt.of(0),DB.get_num_of_column("Reviewer"));
        assertEquals(OptionalInt.of(1),DB.get_num_of_column("Book"));
        assertEquals(OptionalInt.of(2),DB.get_num_of_column("Score"));

    }

    @Test
    public void build_db_3_keys() throws Exception {

        Integer num_of_keys=3;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");
        names_of_columns.add("col3");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3,b");
        data.add("Benny,Harry,9,c");
        data.add("Benny,Harry,9,d");
        data.add("Benny,Bla,8,e");


        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data, false);

        assertEquals(num_of_keys,DB.getNum_of_keys());
        assertEquals(OptionalInt.of(names_of_columns.size()),OptionalInt.of(DB.getNum_of_columns()));
        assertEquals(names_of_columns,DB.getNames_of_columns());
        assertEquals(OptionalInt.of(0),DB.get_num_of_column("Reviewer"));
        assertEquals(OptionalInt.of(1),DB.get_num_of_column("Book"));
        assertEquals(OptionalInt.of(2),DB.get_num_of_column("Score"));
        assertEquals(OptionalInt.of(3),DB.get_num_of_column("col3"));
        assertEquals(OptionalInt.empty(),DB.get_num_of_column("test"));

    }

    @Test
    public void get_val_from_column_by_name_1_key() throws Exception {

        Integer num_of_keys=1;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,9");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data, false);
        List<String> keys1 = new ArrayList<>();
        keys1.add("Nadav");
        List<String> keys2 = new ArrayList<>();
        keys2.add("Michal");
        List<String> keys3 = new ArrayList<>();
        keys3.add("Benny");

        CompletableFuture<Optional<String>> val1=DB.get_val_from_column_by_name(keys1,"Score");
        CompletableFuture<Optional<String>> val2=DB.get_val_from_column_by_name(keys3,"Score");
        CompletableFuture<Optional<String>> val3=DB.get_val_from_column_by_name(keys2,"Score");
        CompletableFuture<Optional<String>> val4=DB.get_val_from_column_by_name(keys1,"Bla");

        assertEquals(Optional.of("3"),val1.get());
        assertEquals(Optional.of("8"),val2.get());
        assertEquals(Optional.empty(),val3.get());
        assertEquals(Optional.empty(),val4.get());

    }

    @Test
    public void get_val_from_column_by_name_2_keys() throws Exception {

        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,9");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<String> keys1 = new ArrayList<>();
        keys1.add("Nadav");
        keys1.add("Harry");
        List<String> keys2 = new ArrayList<>();
        keys2.add("Nadav");
        keys2.add("Bible");

        CompletableFuture<Optional<String>> val1=DB.get_val_from_column_by_name(keys1,"Score");
        CompletableFuture<Optional<String>> val2=DB.get_val_from_column_by_name(keys2,"Score");
        CompletableFuture<Optional<String>> val3=DB.get_val_from_column_by_name(keys1,"Bla");

        assertEquals(Optional.of("8"),val1.get());
        assertEquals(Optional.empty(),val2.get());
        assertEquals(Optional.empty(),val3.get());

    }

    @Test
    public void get_val_from_column_by_name_3_keys() throws Exception {

        Integer num_of_keys=3;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");
        names_of_columns.add("Letter");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3,b");
        data.add("Benny,Harry,9,c");
        data.add("Benny,Harry,9,d");
        data.add("Benny,Bla,8,e");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data, false);
        List<String> keys1 = new ArrayList<>();
        keys1.add("Nadav");
        keys1.add("Harry");
        keys1.add("8");
        List<String> keys2 = new ArrayList<>();
        keys2.add("Nadav");
        keys2.add("Bible");
        keys2.add("8");
        List<String> keys3 = new ArrayList<>();
        keys3.add("Nadav");
        keys3.add("Harry2");
        keys3.add("3");
        List<String> keys4 = new ArrayList<>();
        keys4.add("Nadav");
        keys4.add("Harry2");

        CompletableFuture<Optional<String>> val1=DB.get_val_from_column_by_name(keys1,"Letter");
        CompletableFuture<Optional<String>> val2=DB.get_val_from_column_by_name(keys2,"Letter");
        CompletableFuture<Optional<String>> val3=DB.get_val_from_column_by_name(keys1,"Bla");
        CompletableFuture<Optional<String>> val4=DB.get_val_from_column_by_name(keys4,"Letter");
        CompletableFuture<Optional<String>> val5=DB.get_val_from_column_by_name(keys3,"Letter");


        assertEquals(Optional.of("a"),val1.get());
        assertEquals(Optional.empty(),val2.get());
        assertEquals(Optional.empty(),val3.get());
        assertEquals(Optional.empty(),val4.get());
        assertEquals(Optional.of("b"),val5.get());

    }

    @Test
    public void get_lines_for_key() throws Exception {
    }

    @Test
    public void get_val_from_column_by_column_number_2_keys() throws Exception {


        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,9");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<String> keys1 = new ArrayList<>();
        keys1.add("Nadav");
        keys1.add("Harry");
        List<String> keys2 = new ArrayList<>();
        keys2.add("Nadav");
        keys2.add("Bible");

        CompletableFuture<Optional<String>> val1 = DB.get_val_from_column_by_column_number(keys1,2);
        CompletableFuture<Optional<String>> val2 = DB.get_val_from_column_by_column_number(keys2,2);
        CompletableFuture<Optional<String>> val3 = DB.get_val_from_column_by_column_number(keys2,3);
        CompletableFuture<Optional<String>> val4 = DB.get_val_from_column_by_column_number(keys1,4);


        assertEquals(Optional.of("8"),val1.get());
        assertEquals(Optional.empty(),val2.get());
        assertEquals(Optional.empty(),val3.get());
        assertEquals(Optional.empty(),val4.get());
    }

    @Test
    public void get_lines_for_keys_3_keys() throws Exception{
        Integer num_of_keys=3;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");
        names_of_columns.add("Value");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3,b");
        data.add("Benny,Harry,9,d");
        data.add("Benny,Harry,9,d");
        data.add("Benny,Harry,8,a");
        data.add("Benny,Bla,8,e");


        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<DataBaseElement> values = new ArrayList<>();
        List<DataBaseElement> values2 = new ArrayList<>();
        List<String> keysName = new ArrayList<>();
        List<String> keysName2 = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        List<String> keys3 = new ArrayList<>();

        keysName.add("Reviewer");
        keysName.add("Book");
        keysName2.add("Book");
        keys.add("Benny");
        keys.add("Harry");
        keys3.add("Harry");
        values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        values2.addAll(DB.get_lines_for_keys(keys3,keysName2).get());

        assertEquals("Benny",values.get(0).get("Reviewer"));
        assertEquals("Harry",values.get(0).get("Book"));
        assertEquals("8",values.get(0).get("Score"));
        assertEquals("a",values.get(0).get("Value"));

        assertEquals(3,values2.size());
        assertEquals("Benny",values2.get(0).get("Reviewer"));
        assertEquals("Benny",values2.get(1).get("Reviewer"));
        assertEquals("Nadav",values2.get(2).get("Reviewer"));
        assertEquals("8",values2.get(0).get("Score"));
        assertEquals("9",values2.get(1).get("Score"));
        assertEquals("8",values2.get(2).get("Score"));
        assertEquals("a",values2.get(0).get("Value"));
        assertEquals("d",values2.get(1).get("Value"));
        assertEquals("a",values2.get(2).get("Value"));

        assertEquals("Benny",values.get(1).get("Reviewer"));
        assertEquals("Harry",values.get(1).get("Book"));
        assertEquals("9",values.get(1).get("Score"));
        assertEquals("d",values.get(1).get("Value"));

        values2.addAll(DB.get_lines_for_keys(keys,keysName).get());

        //check if no such entry found
        List<DataBaseElement> empty_values = new ArrayList<>();
        List<String> keys2 = new ArrayList<>();
        keys2.add("Benny");
        keys2.add("80 days");
        empty_values.addAll(DB.get_lines_for_keys(keys2,keysName).get());
        assertTrue(empty_values.size()==0);


        keys.add("Benny");

        try{        //check different array size
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

        keysName.add("NoSuchKey");
        try{        //check that keys name are legal
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}
        keysName.remove("NoSuchKey");

        keysName.add("Book");


        try{        //check that there are to meany keys
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

    }

    @Test
    public void get_lines_for_keys_1_keys() throws Exception{
        Integer num_of_keys=1;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Harry,8");
        data.add("Harry2,4");
        data.add("Harry,9");
        data.add("Harry3,8");
        data.add("Bla5,8");


        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<DataBaseElement> values = new ArrayList<>();
        List<String> keysName = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        keysName.add("Book");
        keys.add("Harry");
        values.addAll(DB.get_lines_for_keys(keys,keysName).get());

       // assertEquals("9", values.get(0));

        assertEquals("Harry",values.get(0).get("Book"));
        assertEquals("9",values.get(0).get("Score"));


        //check if no such entry found
        List<DataBaseElement> empty_values = new ArrayList<>();
        List<String> keys2 = new ArrayList<>();
        keys2.add("NoSuchKey");
        empty_values.addAll(DB.get_lines_for_keys(keys2,keysName).get());
        assertTrue(empty_values.size()==0);


        keys.remove("Harry");

        try{        //check different array size
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

        keysName.remove("Book");
        keysName.add("NoSuchKeyName");
        keys.add("Harry");
        try{        //check that keys name are legal
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

        keysName.remove("NoSuchKey");
        keysName.add("Book");


        try{        //check that there are to meany keys
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

    }

    @Test
    public void get_lines_for_keys_2_keys() throws Exception{
        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,8");
        data.add("Benny,Bla,8");


        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<DataBaseElement> values = new ArrayList<>();
        List<String> keysName = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        keysName.add("Reviewer");
        keys.add("Benny");
        values.addAll(DB.get_lines_for_keys(keys,keysName).get());

        assertEquals(values.get(0).get("Book"), "Bla");
        assertEquals(values.get(1).get("Book"), "Harry");
        assertEquals(values.get(0).get("Score"), "8");
        assertEquals(values.get(1).get("Score"), "8");

        //check if no such entry found
        List<DataBaseElement> empty_values = new ArrayList<>();
        List<String> keys2 = new ArrayList<>();
        keys2.add("NoSuchKey");
        empty_values.addAll(DB.get_lines_for_keys(keys2,keysName).get());
        assertTrue(empty_values.size()==0);
        keys.add("Benny");

        try{        //check different array size
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

        keysName.add("NoSuchKeyName");
        try{        //check that keys name are legal
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}
        keysName.remove("NoSuchKey");

        keysName.add("Book");


        try{        //check that there are to meany keys
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

    }

    @Test
    public void get_lines_for_single_key() throws Exception{
        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,8");
        data.add("Benny,Bla,8");

        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,false);
        List<DataBaseElement> values = new ArrayList<>();
        values.addAll(DB.get_lines_for_single_key("Benny","Reviewer").get());

        assertEquals(values.get(0).get("Book"), "Bla");
        assertEquals(values.get(1).get("Book"), "Harry");
        assertEquals(values.get(0).get("Score"), "8");
        assertEquals(values.get(1).get("Score"), "8");

    }

    @Test
    public void get_lines_for_keys_2_keys_multipels() throws Exception{
        Integer num_of_keys=2;
        List<String> names_of_columns = new ArrayList<>();
        names_of_columns.add("Reviewer");
        names_of_columns.add("Book");
        names_of_columns.add("Score");

        LinkedList<String> data = new LinkedList<>();
        data.add("Nadav,Harry,8,a");
        data.add("Nadav,Harry2,3");
        data.add("Benny,Harry,9");
        data.add("Benny,Harry,8");
        data.add("Benny,Bla,8");


        DataBase DB = SetupAndBuildDataBase(num_of_keys,names_of_columns,data,true);
        List<DataBaseElement> values = new ArrayList<>();
        List<String> keysName = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        keysName.add("Reviewer");
        keys.add("Benny");
        values.addAll(DB.get_lines_for_keys(keys,keysName).get());

        assertEquals(values.get(0).get("Book"), "Bla");
        assertEquals(values.get(1).get("Book"), "Harry");
        assertEquals(values.get(2).get("Book"), "Harry");
        assertEquals(values.get(0).get("Score"), "8");
        assertEquals(values.get(1).get("Score"), "9");
        assertEquals(values.get(2).get("Score"), "8");

        //check if no such entry found
        List<DataBaseElement> empty_values = new ArrayList<>();
        List<String> keys2 = new ArrayList<>();
        keys2.add("NoSuchKey");
        empty_values.addAll(DB.get_lines_for_keys(keys2,keysName).get());
        assertTrue(empty_values.size()==0);
        keys.add("Benny");

        try{        //check different array size
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

        keysName.add("NoSuchKeyName");
        try{        //check that keys name are legal
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}
        keysName.remove("NoSuchKey");

        keysName.add("Book");


        try{        //check that there are to meany keys
            values.addAll(DB.get_lines_for_keys(keys,keysName).get());
        }catch(IllegalArgumentException e){}

    }
}