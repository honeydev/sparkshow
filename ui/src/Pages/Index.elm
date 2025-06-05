module Pages.Index exposing (..)

import Html exposing (..)
import Session exposing (..)

type alias PageModel =
    { session : Session
    }

view model =
    div [] [ text "Index" ]
