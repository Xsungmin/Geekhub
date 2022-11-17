import React, { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import "./css/Nav.css";
import logo from "../../asset/image/logo.png";
import MenuIcon from "@mui/icons-material/Menu";
import HomeIcon from "@mui/icons-material/Home";
import ChatIcon from "@mui/icons-material/Chat";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import SummarizeIcon from "@mui/icons-material/Summarize";
import PersonAddAltIcon from "@mui/icons-material/PersonAddAlt";
import LogoutIcon from "@mui/icons-material/Logout";
import FmdGoodIcon from "@mui/icons-material/FmdGood";
import Logout from "@mui/icons-material/Logout";
// import logo from "../../asset/image/logo.png"
const Nav = () => {
  const [nowActive, setNowActive] = useState("overview");
  const [expanded, setExpanded] = useState(true);
  return (
    // <nav className={`navigation ${expanded && "expanded"}`}>
    <nav className={`sidebar ${expanded && "expanded"}`}>
      <div>
        <Link
          className={"nav-menu"}
          onClick={() => {
            setExpanded(!expanded);
          }}
        >
          <MenuIcon className="nav-icon" />
          <div className={`description ${expanded && "show-description"}`}>
            GeekHub
          </div>
        </Link>
        <Link
          className={"nav-link" + (nowActive === "overview" ? " active" : "")}
          // className={"nav-link"}
          to="/"
          onClick={() => {
            setNowActive("overview");
          }}
        >
          <HomeIcon className="nav-icon" />
          <p className={`description ${expanded && "show-description"}`}>
            Overview
          </p>
        </Link>
        <Link
          className={
            "nav-link" + (nowActive === "driverlocation" ? " active" : "")
          }
          onClick={() => {
            setNowActive("driverlocation");
          }}
          to="driverlocation"
        >
          <LocalShippingIcon className="nav-icon"></LocalShippingIcon>
          <p className={`description ${expanded && "show-description"}`}>
            실시간 모니터링
          </p>
        </Link>
        <Link
          className={"nav-link" + (nowActive === "drivermap" ? " active" : "")}
          onClick={() => {
            setNowActive("drivermap");
          }}
          to="drivermap"
        >
          <FmdGoodIcon className="nav-icon"></FmdGoodIcon>
          <p className={`description ${expanded && "show-description"}`}>
            배달기사 현재위치
          </p>
        </Link>
        <Link
          className={"nav-link" + (nowActive === "chat" ? " active" : "")}
          onClick={() => {
            setNowActive("chat");
          }}
          to="chat"
        >
          <ChatIcon className="nav-icon"></ChatIcon>
          <p className={`description ${expanded && "show-description"}`}>
            채팅
          </p>
        </Link>
        <Link
          className={"nav-link" + (nowActive === "log" ? " active" : "")}
          onClick={() => {
            setNowActive("log");
          }}
          to="log"
        >
          <SummarizeIcon className="nav-icon"></SummarizeIcon>
          <p className={`description ${expanded && "show-description"}`}>
            로그 확인
          </p>
        </Link>
        <Link
          className={"nav-link" + (nowActive === "signup" ? " active" : "")}
          onClick={() => {
            setNowActive("signup");
          }}
          to="signup"
        >
          <PersonAddAltIcon className="nav-icon"></PersonAddAltIcon>
          <p className={`description ${expanded && "show-description"}`}>
            신규 기사 생성
          </p>
        </Link>
      </div>
      <div>
        <Link
          className="logout"
          onClick={() => {
            window.localStorage.setItem("accesstoken", "");
            Navigate("/");
          }}
        >
          <LogoutIcon className="nav-icon"></LogoutIcon>
          <p className={`description ${expanded && "show-description"}`}>
            로그아웃
          </p>
        </Link>
      </div>
    </nav>
  );
};

export default Nav;
