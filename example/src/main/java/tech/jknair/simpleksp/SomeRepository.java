package tech.jknair.simpleksp;

import android.util.Pair;
import java.util.List;
import java.util.Map;

public interface SomeRepository {

    void someFun1(int code, String message);

    void someFun2(List<String> messages);

    void someFun3(Pair<Integer, String> codeMessage);

    void someFun4(Map<SomeOtherRepository, Pair<Integer, String>> aComplecatedMap);

}
