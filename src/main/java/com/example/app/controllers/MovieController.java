package com.example.app.controllers;

import com.example.app.models.*;
import com.example.app.repositories.*;
import com.example.app.services.MovieService;
import com.example.app.types.GetMovieType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
public class MovieController {

    private MovieRepository movieRepository;
    private FanRepository fanRepository;
    private CriticRepository criticRepository;
    private ReviewRepository reviewRepository;
    private ActorRepository actorRepository;

    @Autowired
    public MovieController(MovieRepository movieRepository, FanRepository fanRepository,
                           CriticRepository criticRepository, ReviewRepository reviewRepository,
                           ActorRepository actorRepository) {
        this.movieRepository = movieRepository;
        this.fanRepository = fanRepository;
        this.criticRepository = criticRepository;
        this.reviewRepository = reviewRepository;
        this.actorRepository = actorRepository;
    }

    /**
     * Search TMDb for movies with the given query string
     * @param query a String
     * @return a list of MovieSearchResults
     */
    @GetMapping("/api/search/movies")
    public List<Movie> searchMovies(@RequestParam("query") String query) {
        List<Movie> result = new ArrayList<>();

        if(query.length() != 0) {
            result = MovieService.getMovies(GetMovieType.SEARCH, "en-US", null,
                    query.replace(" ","+"), "1", null);

            for(Movie m : result) {
                movieRepository.save(m);
            }
        }

        return result;
    }

    /**
     * Get list of Top Rated Movies from TMDb for a particular region
     * @param region a String
     * @return a list of MovieSearchResults
     */
    @GetMapping("/api/movies/top_rated")
    public List<Movie> getTopRatedMovies(@RequestParam(value = "region", defaultValue = "us") String region,
                                              @RequestParam(value = "lang", defaultValue = "en-US") String lang,
                                              @RequestParam(value = "page", defaultValue = "1") String pageNo) {
        List<Movie> result = MovieService.getMovies(GetMovieType.TOP_RATED, lang, region, null, pageNo, null);
                //.stream().limit(3).collect(Collectors.toList());

        for(Movie m : result) {
            movieRepository.save(m);
        }

        return result;
    }

    /**
     * Get list of Now Playing Movies from TMDb for a particular region
     * @param region a String
     * @return a list of MovieSearchResults
     */
    @GetMapping("/api/movies/now_playing")
    public List<Movie> getNowPlayingMovies(@RequestParam(value = "region", defaultValue = "us") String region,
                                                @RequestParam(value = "lang", defaultValue = "en-US") String lang,
                                                @RequestParam(value = "page", defaultValue = "1") String pageNo) {
        List<Movie> result = MovieService.getMovies(GetMovieType.NOW_PLAYING, lang, region, null, pageNo, null);
                //.stream().limit(3).collect(Collectors.toList());

        for(Movie m : result) {
            movieRepository.save(m);
        }

        return result;
    }

    /**
     * Get list of Popular Movies from TMDb for a particular region
     * @param region a String
     * @return a list of MovieSearchResults
     */
    @GetMapping("/api/movies/popular")
    public List<Movie> getPopularMovies(@RequestParam(value = "region", defaultValue = "us") String region,
                                             @RequestParam(value = "lang", defaultValue = "en-US") String lang,
                                             @RequestParam(value = "page", defaultValue = "1") String pageNo) {
        List<Movie> result = MovieService.getMovies(GetMovieType.POPULAR, lang, region, null, pageNo, null);
                //.stream().limit(3).collect(Collectors.toList());

        for(Movie m : result) {
            movieRepository.save(m);
        }

        return result;
    }

    /**
     * Get list of Upcoming Movies from TMDb for a particular region
     * @param region a String
     * @return a list of MovieSearchResults
     */
    @GetMapping("/api/movies/upcoming")
    public List<Movie> getUpcomingMovies(@RequestParam(value = "region", defaultValue = "us") String region,
                                              @RequestParam(value = "lang", defaultValue = "en-US") String lang,
                                              @RequestParam(value = "page", defaultValue = "1") String pageNo) {
        List<Movie> result = MovieService.getMovies(GetMovieType.UPCOMING, lang, region, null, pageNo, null);
                //.stream().limit(3).collect(Collectors.toList());

        for(Movie m : result) {
            movieRepository.save(m);
        }

        return result;
    }

    /**
     * Get list of recommended movies for given Fan
     * @param id fan id
     * @return list of Movies
     */
    @GetMapping("/api/fan/{id}/movies/recommended")
    public List<Movie> getMoviesRecommendedForFan(@PathVariable("id") Long id) {
        List<Movie> result = new ArrayList<>();
        Fan fan = fanRepository.findById(id).orElse(null);

        if(fan != null) {
            List<Critic> criticsFollowed = fan.getCriticsFollowed();
            List<Fan> fansFollowed = fan.getFollowingFans();

            // Add movies recommended by followed critics if not already liked
            for(Critic c: criticsFollowed) {
                for(Movie recommendedMovie: c.getRecommendedMovies()) {
                    if(!fan.getLikesMovies().contains(recommendedMovie)) {
                        result.add(recommendedMovie);
                    }
                }
            }

            // Add movies liked by followed fans if not already liked
            for(Fan f: fansFollowed) {
                for(Movie likedMovie: f.getLikesMovies()) {
                    if(!fan.getLikesMovies().contains(likedMovie)) {
                        result.add(likedMovie);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Retrieve the TMDb movie with the given id
     * @param id a Long
     * @return the Movie with the given id
     */
    @GetMapping("/api/movies/{id}")
    Movie findMovieById(@PathVariable("id") Long id) {
        Movie m = MovieService.findMovieById(id);
        movieRepository.save(m);
        return m;
    }

    @GetMapping("/api/movies")
    public List<Movie> findAllMovie(){
        return movieRepository.findAll();
    }

    @PostMapping("/api/movie")
    public Movie createMovie(@RequestBody Movie movie){
        return movieRepository.save(movie);
    }

    @PostMapping("/api/like/movie/{movieId}/fan/{username}")
    public void likeMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && fanRepository.findById(fanRepository.findFanIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(username)).get();
            movie.likedByFan(fan);
            movieRepository.save(movie);
        }
    }

    @GetMapping("/api/check/like/fan/{username}/movie/{movieId}")
    public Boolean checkIfFanLikesMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && fanRepository.findById(fanRepository.findFanIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(username)).get();
            List<Fan> fansWhoLike = movie.getLikedByFans();

            if(fansWhoLike.contains(fan))
            {
                return true;
            }
        }

        return false;
    }

    @PostMapping("/api/dislike/movie/{movieId}/fan/{username}")
    public void dislikeMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && fanRepository.findById(fanRepository.findFanIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(username)).get();
            movie.dislikedByFan(fan);
            movieRepository.save(movie);
        }
    }

    @GetMapping("/api/check/dislike/fan/{username}/movie/{movieId}")
    public Fan checkIfFanDislikesMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && fanRepository.findById(fanRepository.findFanIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(username)).get();
            List<Fan> fansWhoDisliked = movie.getDislikedByFans();

            if(fansWhoDisliked.contains(fan))
            {
                return fan;
            }
        }

        return null;
    }

    @PostMapping("/api/recommend/movie/{movieId}/critic/{username}")
    public void recommendMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            movie.recommendedByCritic(critic);
            movieRepository.save(movie);
        }
    }

    @GetMapping("/api/check/recommend/critic/{username}/movie/{movieId}")
    public Critic checkIfCriticRecommendsMovie (
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            List<Critic> criticsWhoRecommended = movie.getRecommendedBy();

            if(criticsWhoRecommended.contains(critic))
            {
                return critic;
            }
        }

        return null;
    }

    @PostMapping("/api/reviews/movie/{movieId}/review/{reviewId}")
    public void reviewMovie(
            @PathVariable("movieId") long movieId,
            @PathVariable("reviewId") long reviewId) {
        if(movieRepository.findById(movieId).isPresent()
                && reviewRepository.findById(reviewId).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Review review = reviewRepository.findById(reviewId).get();
            movie.hasReviews(review);
            movieRepository.save(movie);
        }
    }

    @GetMapping("/api/recommend/movie/{movieId}/recommendedby")
    public List<Critic> listOfCriticsRecommended(
            @PathVariable("movieId") long movieId){
        if(movieRepository.findById(movieId).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            return movie.getRecommendedBy();
        }
        return null;
    }

    @GetMapping("/api/like/movie/{movieId}/likedbyfans")
    public List<Fan> listOfFansLikedMovie(
            @PathVariable("movieID") long movieId){
        if(movieRepository.findById(movieId).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            return movie.getLikedByFans();
        }
        return null;
    }

    @GetMapping("/api/dislike/movie/{movieId}/dislikedbyfans")
    public List<Fan> listOfFansDislikedMovie(
            @PathVariable("movieId") long movieId){
        if(movieRepository.findById(movieId).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            return movie.getDislikedByFans();
        }
        return null;
    }

    @GetMapping("/api/movie/{movieId}/reviews")
    public List<Review> listOfReviews(
            @PathVariable("movieId") long movieId){
        if(movieRepository.findById(movieId).isPresent()){
            Movie movie = movieRepository.findById(movieId).get();
            return movie.getMovieReview();
        }
        return null;
    }

    @GetMapping("/api/movie/{movieId}/cast")
    public List<Actor> getMovieCast (@PathVariable("movieId") long movieId){
        if(movieRepository.findById(movieId).isPresent()){
            Movie movie = movieRepository.findById(movieId).get();
            return movie.getListOfActors();
        }
        return null;
    }
}
