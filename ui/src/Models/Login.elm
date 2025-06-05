module Models.Login exposing (..)

import Session exposing (Session)

type alias PageModel =
    { username : String
    , password : String
    , session : Session
    }


type alias Form =
    { username : String
    , password : String
    }


init : Session -> PageModel
init s =
    { username = "", password = "", session = s }
