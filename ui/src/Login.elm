module Login exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (Decoder, field, int, map2, string)
import Session exposing (..)
import Url


type alias Model =
    { username : String
    , password : String
    , session : Session
    }


type alias Form =
    { username : String
    , password : String
    }


init : Session -> Model
init s =
    { username = "", password = "", session = s }


type Msg
    = ChangeUsername String
    | ChangePassword String
    | SendForm
    | PageOpened
    | GetResult (Result Http.Error Response)


type alias Response =
    { status : String
    , token : String
    }


sendForm : Cmd Msg
sendForm =
    Http.get
        { url = "/api/v1/auth"
        , expect = Http.expectJson GetResult formDecoder
        }


formDecoder : Decoder Response
formDecoder =
    map2 Response
        (field "status" string)
        (field "token" string)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangeUsername username ->
            ( { model | username = username }
            , Cmd.none
            )

        ChangePassword password ->
            ( { model | password = password }
            , Cmd.none
            )

        SendForm ->
            ( model, sendForm )

        PageOpened ->
            ( model, Cmd.none )

        GetResult response ->
            ( model, Cmd.none )


view model =
    div []
        [ label [] [ text "Username" ]
        , input [ value model.username, onInput ChangeUsername ] []
        , label [] [ text "Password" ]
        , input [ type_ "password", value model.password, onInput ChangePassword ] []
        , button [ onClick SendForm ] [ text <| Debug.toString model ]
        ]
