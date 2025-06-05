module Main exposing (..)

import Browser
import Browser.Navigation as Nav
import Components.Navbar as Navbar
import Html exposing (..)
import Html.Attributes exposing (..)
import Pages.Index as IndexPage
import Pages.Login as LoginPage
import Models.Login
import Models.Model exposing (Model)
import Models.PageModel exposing (PageModel(..))
import Models.Login as LoginModel
import Pages.NotFound as NotFound
import Route exposing (Route)
import Session exposing (..)
import Url



-- MAIN


main : Program (Maybe String) Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = LinkClicked
        }

-- MODEL

getSession : Model -> Session
getSession model =
    case model.pageModel of
        IndexPageModel m ->
            m.session

        NotFoundPageModel ->
            Unauthenticated

        LoginPageModel m ->
            m.session




init : Maybe String -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        -- FIXME: remove this
        store =
            storeSession { token = "abc" }

        route =
            Route.parseUrl url

        page =
            case route of
                Route.Index ->
                    IndexPageModel { session = Unauthenticated }

                Route.Login ->
                    LoginModel.init Unauthenticated |> LoginPageModel

                Route.NotFound ->
                    NotFoundPageModel

        _ =
            Debug.toString page |> Debug.log "Page"
    in
    ( { pageModel = page, route = route, navKey = key, session = Unauthenticated }
    , store
    )



-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoginMessage LoginPage.Msg


authHook msg model =
    case model of
        IndexPageModel session ->
            case session of
                --Unauthenticated ->
                --    LoginMessage LoginPage.PageOpened
                _ ->
                    msg

        NotFoundPageModel ->
            msg

        LoginPageModel subpageModel ->
            msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    let
        hookedMsg =
            authHook msg model.pageModel

        _ =
            Debug.toString hookedMsg |> Debug.log "Hooked"
    in
    case hookedMsg of
        UrlChanged url ->
            let
                route =
                    Route.parseUrl url

                page =
                    case route of
                        Route.Index ->
                            IndexPageModel
                             { session = Unauthenticated }

                        Route.Login ->
                            LoginModel.init Unauthenticated |> LoginPageModel

                        Route.NotFound ->
                            NotFoundPageModel

                _ =
                    Debug.toString page |> Debug.log "Page"
            in
            ( { model |  pageModel = page, route = route }, Cmd.none )

        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Nav.pushUrl model.navKey (Url.toString url) )

                Browser.External url ->
                    ( model, Nav.load url )

        LoginMessage subMsg ->
            let
                mod = case model.pageModel of
                    LoginPageModel mold -> mold
                    _ -> LoginModel.init (getSession model)

                ( m, cmd ) =
                    -- LoginPage.update subMsg (LoginPage.init (getSession model))
                    LoginPage.update subMsg model mod
            in
            ( { model | pageModel = LoginPageModel m, route = Route.Login, navKey = model.navKey }
            , Cmd.map LoginMessage cmd
            )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = "URL Interceptor"
    , body =
        [ ul []
            [ Navbar.build
                [ Navbar.Link "Query" "query"
                , Navbar.Link "Profile" "profile"
                , case model.session of
                    Unauthenticated -> Navbar.Link "Login" "login"
                    Active _        -> Navbar.Link "Sign out" "sign-out"
                ]
            ]
        , case model.pageModel of
            LoginPageModel m ->
                Html.map LoginMessage (LoginPage.view m)

            NotFoundPageModel ->
                NotFound.view

            IndexPageModel m ->
                IndexPage.view m
        ]
    }


viewLink : String -> Html msg
viewLink path =
    li [] [ a [ href path ] [ text path ] ]
