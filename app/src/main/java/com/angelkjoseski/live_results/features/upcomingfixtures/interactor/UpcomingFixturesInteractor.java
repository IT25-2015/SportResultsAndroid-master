package com.angelkjoseski.live_results.features.upcomingfixtures.interactor;

import com.angelkjoseski.live_results.features.common.interactor.InteractorTemplate;
import com.angelkjoseski.live_results.features.upcomingfixtures.UpcomingFixtures;
import com.angelkjoseski.live_results.model.Fixture;
import com.angelkjoseski.live_results.model.FixtureList;
import com.angelkjoseski.live_results.service.FavouriteService;
import com.angelkjoseski.live_results.service.networking.ApiService;
import com.angelkjoseski.live_results.util.rx_transformers.FillFixturesWithTeamDetailsTransformer;
import com.angelkjoseski.live_results.util.rx_transformers.FixturesBasedOnFavouritesTransformer;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Interactor with business-logic for 'All fixtures' screen.
 */
public class UpcomingFixturesInteractor extends InteractorTemplate<FixtureList> implements UpcomingFixtures.Interactor {

    private final FavouriteService favouriteService;

    /**
     * Constructor for injecting REST API service and Retrofit instance.
     *
     * @param apiService implementation of REST API service
     * @param retrofit   singleton Retrofit instance
     */
    @Inject
    public UpcomingFixturesInteractor(ApiService apiService, Retrofit retrofit, FavouriteService favouriteService) {
        super(apiService, retrofit);
        this.favouriteService = favouriteService;
    }

    @Override
    public Observable<List<Fixture>> getAllUpcomingFixtures() {
        return apiService.getAllFixtures()
                .subscribeOn(Schedulers.io())
                .flatMap(fixtureList -> Observable.fromIterable(fixtureList.getFixtures()))
                .filter(fixture -> new Date().before(fixture.getStartTime()))
                .toList()
                .toObservable()
                .compose(new FillFixturesWithTeamDetailsTransformer(apiService))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Fixture>> getUpcomingFixturesForFavouriteTeams() {
        return apiService.getAllFixtures()
                .subscribeOn(Schedulers.io())
                .map(fixtureList -> fixtureList.getFixtures())
                .compose(new FixturesBasedOnFavouritesTransformer(favouriteService))
                .filter(fixture -> new Date().before(fixture.getStartTime()))
                .toList()
                .toObservable()
                .compose(new FillFixturesWithTeamDetailsTransformer(apiService))
                .observeOn(AndroidSchedulers.mainThread());
    }
}
