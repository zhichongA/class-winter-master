package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;

import java.util.Objects;

/**
 * (non-javadoc)
 *
 * @author {@link JustryDeng}
 * @since 2021/6/5 11:19:21
 */
public class Pair<K, V> {
    
    private final K left;
    
    private final V right;
    
    private Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }
    
    public K getLeft() {
        return this.left;
    }
    
    public V getRight() {
        return this.right;
    }
    
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
    
    @Override
    public String toString() {
        return "Pair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
