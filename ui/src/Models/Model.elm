module Models.Model exposing (..)

import Browser.Navigation as Nav
import Models.PageModel exposing (PageModel)
import Route exposing (Route)
import Session exposing (Session)

type alias Model =
    { pageModel : PageModel
    , route : Route
    , navKey : Nav.Key
    , session : Session
    }

