import React, { useState, useEffect } from "react";
import "./css/List.css";
import PropTypes from "prop-types";
import Box from "@mui/material/Box";
import Collapse from "@mui/material/Collapse";
import IconButton from "@mui/material/IconButton";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Paper from "@mui/material/Paper";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import TablePagination from "@mui/material/TablePagination";
import Swal from "sweetalert2";
import Toast from "../../utils/Toast";
import { apiInstance } from "../../api/index";
import logoDotBlack from "../../asset/image/logo-dot-black.png";
import logoDotWhite from "../../asset/image/logo-dot-white.png";
import {
  Map,
  MapMarker,
  CustomOverlayMap,
  Polyline,
} from "react-kakao-maps-sdk";
import markerImg from "../../asset/image/marker.png";

const List = (props) => {
  const listData = props.listData;
  const selected = props.selected;

  function Row(props) {
    const { row } = props;
    const [open, setOpen] = React.useState(false);
    const [logData, setLogData] = useState([]);
    const [dataMap, setDataMap] = useState([]);
    const [state, setState] = useState({
      center: { lat: 35.19919101818564, lng: 126.87300478078876 },
      isPanto: false,
      level: 7,
    });
    const [firstLat, setFirstLat] = useState(35.19919101818564);
    const [firstLng, setFirstLng] = useState(126.87300478078876);
    const [firstName, setFirstName] = useState();
    const API = apiInstance();
    let allTask = 0;
    let completeTask = 0;
    {
      row.spotResponseDtoList.map((c) => {
        if (c.status === 2) {
          allTask += 1;
          completeTask += 1;
        } else if (c.status === 1) {
          allTask += 1;
        }
      });
    }
    async function getLog50(id) {
      console.log(id);
      console.log(selected.date);
      // ?????? ?????? Get ?????? ????????? ?????? ?????? ?????? ?????????
      // ?????? ?????? ?????? ?????? ??? ?????? ?????? ??????
      if (allTask > 0) {
        try {
          const res = await API.get("/location/getLog50", {
            params: {
              driver: id,
              date: selected.date,
            },
          });
          console.log(res.data);
          let result = [];
          for (let i = 0; i < res.data.length; i++) {
            let item = res.data[i];
            if (!item.latitude) continue;
            setFirstLat(item.latitude);
            setFirstLng(item.longitude);
            if (i % 5 != 0) continue;
            result.push({
              lat: item.latitude,
              lng: item.longitude,
            });
          }
          console.log(result);
          setDataMap(result);
          setLogData(res.data);
          setOpen(!open);
        } catch (err) {
          console.log(err);
        }
      } else {
        Toast.fire({
          icon: "info",
          title: "????????? ???????????? ????????????.",
          timer: 1000,
          position: "center",
        });
      }
    }

    async function getLog(id) {
      try {
        const res = await API.get("/location/getLog", {
          params: {
            driver: id,
            date: selected.date,
          },
        });
        console.log(res.data);
        setLogData(res.data);
        setOpen(!open);
      } catch (err) {
        console.log(err);
      }
    }
    return (
      <React.Fragment>
        <TableRow
          sx={{
            "& > *": {
              borderBottom: "unset",
            },
            "&:hover": {
              color: "#10b981",
              backgroundColor: "rgba( 0, 0, 0, 0.08 )",
            },
          }}
        >
          <TableCell
            sx={{
              width: "10%",
            }}
          >
            <IconButton
              aria-label="expand row"
              size="small"
              onClick={() => {
                setFirstName(row.userName);
                getLog50(row.userIdx);
              }}
            >
              {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
            </IconButton>
          </TableCell>
          <TableCell
            component="th"
            scope="row"
            onClick={() => {
              // ????????? ?????? ???????????? ??????(get(userIdx))
              console.log(row);
              setFirstName(row.userName);
              getLog50(row.userIdx);
            }}
            sx={{
              cursor: "pointer",
            }}
          >
            {allTask === 0 ? (
              <div className="name-container">
                <div className="list-username">{row.userName}</div>
                <div className="no-list">????????? ???????????? ????????????.</div>
              </div>
            ) : (
              <div className="name-container">
                <div className="list-username">{row.userName}</div>
                <div className="no-list">
                  ??? {completeTask}?????? ????????? ????????????.
                </div>
              </div>
            )}
          </TableCell>
        </TableRow>
        <TableRow>
          <TableCell
            style={{ paddingBottom: 0, paddingTop: 0 }}
            colSpan={6}
            className="typography-container"
          >
            <Collapse in={open} timeout="auto" unmountOnExit>
              <Box sx={{ margin: 1 }}>
                <div>
                  <Typography
                    variant="h5"
                    gutterBottom
                    component="div"
                    sx={{ margin: "0.5rem" }}
                  >
                    ??????
                  </Typography>
                  <div className="img-container">
                    {row.spotResponseDtoList.map((taskRow) => (
                      <div className="img-box">
                        {taskRow.imageUrl ? (
                          <>
                            <img
                              className="img"
                              src={taskRow.imageUrl}
                              onClick={() => {
                                const spotName = taskRow.spotName;
                                const imageUrl = taskRow.imageUrl;
                                const arrivedTime = taskRow.arrivedTime;
                                Swal.fire({
                                  title: spotName,
                                  text: `?????? ?????? : ${arrivedTime.slice(
                                    11,
                                    16
                                  )}`,
                                  imageUrl: imageUrl,
                                  imageWidth: 300,
                                  imageHeight: 300,
                                  imageAlt: "Pickup image",
                                });
                              }}
                            />
                            <h3>{taskRow.spotName}</h3>
                          </>
                        ) : (
                          <img className="img" src={logoDotBlack}></img>
                        )}
                      </div>
                    ))}
                  </div>

                  <Typography
                    variant="h5"
                    gutterBottom
                    component="div"
                    sx={{ margin: "0.5rem" }}
                  >
                    GPS
                  </Typography>
                  <span
                    className="all-show"
                    onClick={() => {
                      getLog(row.userIdx);
                    }}
                  >
                    ?????? ?????? ?????? ??????
                  </span>
                  <div className="grid-box">
                    <Map
                      center={{
                        // ????????? ????????????
                        lat: firstLat,
                        lng: firstLng,
                      }}
                      style={{
                        // ????????? ??????
                        width: "100%",
                        height: "450px",
                      }}
                      level={5} // ????????? ?????? ??????
                    >
                      <Polyline
                        path={dataMap}
                        strokeWeight={10} // ?????? ?????? ?????????
                        strokeColor={"red"} // ?????? ???????????????
                        strokeOpacity={0.7} // ?????? ???????????? ????????? 1?????? 0 ????????? ????????? 0??? ??????????????? ???????????????
                        strokeStyle={"solid"} // ?????? ??????????????????
                      />
                      <CustomOverlayMap // ????????? ??????????????? ????????? Container
                        // ????????? ??????????????? ????????? ???????????????
                        position={{
                          lat: firstLat,
                          lng: firstLng,
                        }}
                      >
                        {/* ????????? ??????????????? ????????? ??????????????? */}
                        <div className="label" style={{ color: "#0e1737" }}>
                          <span className="label-span">{firstName}</span>
                        </div>
                      </CustomOverlayMap>
                      <MapMarker
                        position={{
                          lat: firstLat,
                          lng: firstLng,
                        }} // ????????? ????????? ??????
                        title={firstName} // ????????? ?????????, ????????? ???????????? ????????? ???????????? ???????????????
                        image={{
                          src: markerImg,
                          size: {
                            width: 24,
                            height: 35,
                          },
                        }}
                      ></MapMarker>
                    </Map>

                    <Table
                      size="small"
                      aria-label="purchases"
                      className="table-container"
                    >
                      <TableHead>
                        <TableRow>
                          <TableCell>??????</TableCell>
                          <TableCell>??????</TableCell>
                          <TableCell>??????</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {logData.map((log) => (
                          <TableRow key={log._id}>
                            <TableCell component="th" scope="row">
                              {JSON.stringify(log.timestamp)}
                            </TableCell>
                            <TableCell>
                              {JSON.stringify(log.latitude)}
                            </TableCell>
                            <TableCell>
                              {JSON.stringify(log.longitude)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                </div>
              </Box>
            </Collapse>
          </TableCell>
        </TableRow>
      </React.Fragment>
    );
  }

  const rows = listData;
  return (
    <TableContainer component={Paper} className="table-container">
      <Table aria-label="collapsible table">
        <TableBody>
          {rows.map((row) => (
            <Row key={row.name} row={row} />
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default List;
