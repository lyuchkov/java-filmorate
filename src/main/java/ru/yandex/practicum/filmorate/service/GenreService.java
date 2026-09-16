package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreNotFoundException;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreService {
    GenreStorage genreStorage;

    public Collection<Genre> getAll() {
        return this.genreStorage.getAll();
    }

    public Genre getGenre(int id) {
        log.info("Get: Genre with id {}", id);
        return this.genreStorage.getGenreById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));
    }

    public List<Genre> getGenres(Collection<Integer> genreIds) {
        log.info("Get: Genres with ids {}", genreIds);
        List<Genre> genres = this.genreStorage.getGenresByIds(genreIds);

        if (genres.size() != genreIds.size()) {
            log.warn("One or more genres not found");
            throw new GenreNotFoundException(genreIds.stream().mapToInt(Integer::intValue).toArray());
        }

        return genres;
    }
}