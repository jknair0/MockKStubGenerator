package tech.jknair.simpleksp;

import android.util.Pair;
import java.util.List;
import java.util.Map;

public interface SomeRepository {

    void someFun1(int code, String message);

    Response<DataClass> someFun2(List<String> messages);

    void someFun3(Pair<Integer, String> codeMessage);

    void someFun4(Map<SomeOtherRepository, Pair<Integer, String>> aComplecatedMap);

    class Response<T> {

        private final T data;
        private final int error;

        public Response(T data, int error) {
            this.data = data;
            this.error = error;
        }

        public T getData() {
            return data;
        }

        public int getError() {
            return error;
        }
    }

    class DataClass {

        private final int someDataInt;
        private final String someDataString;

        public DataClass(int someDataInt, String someDataString) {
            this.someDataInt = someDataInt;
            this.someDataString = someDataString;
        }
    }

}
