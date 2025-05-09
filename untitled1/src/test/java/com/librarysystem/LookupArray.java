// ============== File: com/librarysystem/LookupArray.java (POPRAWIONY) ==============
package com.librarysystem;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LookupArray implements IPresent {
    private Map<String, Book> presenceMap;
    private IReadWrite dataSource; // Storage

    public LookupArray(IReadWrite dataSource) {
        this.dataSource = dataSource;
        this.presenceMap = new HashMap<>();
        refreshPresenceMap(); // Załaduj stan początkowy
    }

    // Metoda do odświeżania mapy na podstawie dataSource
    // Może być publiczna, jeśli inne komponenty potrzebują wymusić odświeżenie
    public void refreshPresenceMap() {
        this.presenceMap.clear();
        if (this.dataSource != null) { // Dodatkowe sprawdzenie
            for (Book book : dataSource.getAllBooks()) {
                if (book != null && book.getTitle() != null && book.getAuthor() != null) { // Sprawdzenie nulli
                    presenceMap.put(generateKey(book.getTitle(), book.getAuthor()), book);
                }
            }
        }
        // System.out.println("LookupArray: Refreshed presence map. Size: " + presenceMap.size());
    }

    private String generateKey(String title, String author) {
        return title.toLowerCase() + "#" + author.toLowerCase();
    }

    @Override
    public void registerBook(Book book) {
        // IPresent.registerBook powinien tylko zaktualizować swój wewnętrzny stan (presenceMap)
        // Zakładamy, że Executor już zarejestrował książkę w IReadWrite (Storage)
        if (book != null && book.getTitle() != null && book.getAuthor() != null) { // Sprawdzenie nulli
            presenceMap.put(generateKey(book.getTitle(), book.getAuthor()), book);
            System.out.println("LookupArray: Noted book '" + book.getTitle() + "'. Map size: " + presenceMap.size());
        }
        // NIE wołamy tutaj dataSource.registerBook(book);
        // refreshPresenceMap(); // Alternatywnie, zamiast dodawać pojedynczo, można odświeżyć całość
    }

    @Override
    public boolean isPresent(String title, String author) {
        // Dla pewności, że pracujemy na najświeższych danych, można tu odświeżyć mapę,
        // ale może to być kosztowne. Lepsze jest, gdy Executor informuje o zmianach.
        // refreshPresenceMap(); // Rozważ, czy to konieczne przy każdym isPresent
        return presenceMap.containsKey(generateKey(title, author));
    }

    @Override
    public void removeBook(String title, String author) {
        // IPresent.removeBook powinien tylko zaktualizować swój wewnętrzny stan
        // Zakładamy, że Executor już usunął książkę z IReadWrite (Storage)
        Book removed = presenceMap.remove(generateKey(title, author));
        if (removed != null) {
            System.out.println("LookupArray: De-listed book '" + title + "' by " + author + ". Map size: " + presenceMap.size());
        }
        // NIE wołamy tutaj dataSource.removeBook(...);
        // refreshPresenceMap(); // Alternatywnie
    }

    @Override
    public List<Book> getPresentableBooks() {
        // IPresent powinien zwracać książki ze swojego źródła danych,
        // potencjalnie po jakimś filtrowaniu lub transformacji.
        // W tym przypadku, po prostu deleguje do dataSource i upewnia się, że jego
        // wewnętrzna mapa jest w miarę aktualna (chociaż lepsze byłoby odświeżanie przy zmianach).
        // refreshPresenceMap(); // Aby mieć pewność, że zwracamy aktualne dane
        return dataSource.getAllBooks(); // Zwraca listę książek ze Storage
    }
}