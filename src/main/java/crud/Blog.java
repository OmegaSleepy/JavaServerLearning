package crud;

import org.jetbrains.annotations.NotNull;

public record Blog (int id, String title, String tag, String excerpt, String content) {

    @NotNull
    @Override
    public String toString() {
        return title + " " + tag + " " + excerpt + " " + content;
    }
}
