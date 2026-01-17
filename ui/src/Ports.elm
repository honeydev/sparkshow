port module Ports exposing (..)

import Platform.Cmd exposing (Cmd)


port storeSession : String -> Cmd msg


port loadSession : (String -> msg) -> Sub msg


port removeLocalStorageItem : String -> Cmd msg
