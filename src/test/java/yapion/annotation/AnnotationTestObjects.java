package yapion.annotation;

import yapion.annotations.deserialize.YAPIONLoad;
import yapion.annotations.object.*;
import yapion.annotations.serialize.YAPIONOptimize;
import yapion.annotations.serialize.YAPIONSave;

public class AnnotationTestObjects {

    @YAPIONData
    public static class PreTest {

        @YAPIONPreDeserialization(context = {"exception"})
        @YAPIONPreSerialization(context = {"exception"})
        private void pre() {
            throw new UnsupportedOperationException();
        }

        @YAPIONPreDeserialization(context = {"noException"})
        @YAPIONPreSerialization(context = {"noException"})
        private void preOther() {

        }

    }

    @YAPIONData
    public static class PostTest {

        @YAPIONPostDeserialization(context = {"exception"})
        @YAPIONPostSerialization(context = {"exception"})
        private void post() {
            throw new UnsupportedOperationException();
        }

        @YAPIONPostDeserialization(context = "noException")
        @YAPIONPostSerialization(context = {"noException"})
        private void postOther() {

        }

    }

    @YAPIONData
    public static class OptimizeTest {

        @YAPIONOptimize
        private final String s;

        public OptimizeTest(String s) {
            this.s = s;
        }

    }

    @YAPIONObjenesis
    @YAPIONSave
    @YAPIONLoad
    public static class ObjenesisTest {

        @YAPIONSave
        @YAPIONLoad(context = {"objenesis"})
        private final String s = "objenesis";

    }

    @YAPIONData(context = "type")
    @YAPIONSave(context = "fieldOther")
    public static class FieldTypeTest1 {

        @YAPIONField(context = {"fieldOther"})
        private final String s = "some-string";

    }

    @YAPIONSave(context = "other")
    public static class FieldTypeTest2 {

        @YAPIONField(context = {"fieldOther"})
        private final String s = "some-string";

    }

    @YAPIONData
    public static class ClassC extends ClassB {

        @YAPIONPreSerialization(context = "Test")
        private void test() {

        }

    }

    @YAPIONData
    public static class ClassB extends ClassA {

        @YAPIONPreSerialization
        private void test() {

        }

    }

    public static class ClassA {

        @YAPIONPreSerialization
        private void test2() {
            throw new UnsupportedOperationException();
        }

    }

}
