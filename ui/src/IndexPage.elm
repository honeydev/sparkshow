module IndexPage exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Session exposing (..)


type alias Model =
    { session : Session
    }


view model =
    div [] [ text "Index" ]
